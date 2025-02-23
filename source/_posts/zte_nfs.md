---
title: 【硬件】中兴摄像头挂载任意NFS
categories: 硬件
tags: nfs
date: 2019.03.14 00:24:03
---
### 背景
> 家里的刚刚装了一台中兴摄像头。视频是存储到内存卡上的。我就想，如果别人把内存卡拔了，视频就完全丢失了。想存储到NAS上，但是，家里唯一一台用作存储的路由器已经拿去档口用了，暂时没有低成本，低功耗的方案。刚刚好我有几台存储闲置的服务器。

<!--### 问题
- **app只能设置连接上互联网的设备。**打电话问了中兴的工程师，给了我这样的答复。其实我想问，有没有其他本地的入口。但是他并没有告诉我。-->

#### 前文
- **能帮助你什么**。如果你不想购买中兴的云存储服务，并且有一台可以用作存储的linux机器，并且想把摄像头的视频存储到该机器上的。
- **思路**。我登入了摄像头的后台，添加NAS的时候，发现是支持NFS的。所以，我们可以在服务器挂载NFS，然后在摄像头添加这个NFS。
- **物料准备**。一台centos的机器，一个中兴摄像头（能够访问服务器）。
#### 服务器部分，安装及配置NFS
- **使用 yum 安装 NFS 安装包。**
```
sudo yum install nfs-utils
```
- **设置 NFS 服务开机启动**
```
$ sudo systemctl start rpcbind
$ sudo systemctl start nfs
```
- **防火墙允许 rpc-bind 和 nfs 的服务**，如果没有开启防火墙请忽略。
```
$ sudo firewall-cmd --zone=public --permanent --add-service=rpc-bind
success
$ sudo firewall-cmd --zone=public --permanent --add-service=mountd
success
$ sudo firewall-cmd --zone=public --permanent --add-service=nfs
success
$ sudo firewall-cmd --reload
success
```
- 设置用于共享的目录。**文件名随便取**
```
$ sudo mkdir /usr/nfs-share
$ sudo chmod 777 /usr/nfs-share
```
- 配置NFS
```
$ sudo vi /etc/exports

/****在文件添加以下配置****/
/usr/nfs-share/    192.168.0.0/24(rw,sync,no_root_squash,no_all_squash)
```
**！！记住IP后面权限设置是不带空格的！！否则不会设置权限会报错！！**
| 参数           | 说明                                                              |
|----------------|-------------------------------------------------------------------|
| /usr/nfs-share | 共享目录位置                                                      |
| 192.168.1.0/24 | 客户端 IP 范围，* 代表所有，即没有限制。你可以这样写：192.168.1.* |
| rw             | 读写权限                                                          |
| sync           | 同步共享目录。                                                    |
| no_root_squash | 可以使用 root 授权                                                |
| no_all_squash  | 可以使用普通用户授权                                              |
- 重启NFS，检查NFS服务。到此为止，我们的服务端配置完毕。
```
/*重启*/
$ sudo systemctl restart nfs

/*检查服务*/
$ showmount -e
/*
Export list for localhost:
/data 192.168.0.0/24
*/
```
### 监控部分，设置NAS
- **可以通过网页设置监控，找到监控的IP**。我登录了路由器的后台，查看了连接设备。排除了电脑/手机等设备。剩下的就是监控不会错了。
![image.png](/img/hardware/14.webp)

- **获取登录密码**，使用手机扫码，添加摄像头。添加成功后。打开**设置**、**通用**、**初始化网络密码**（下面图片是英文的，不过都一样的）。然后会出现下右图的密码。长按就可以复制了。
![image.png](/img/hardware/15.webp)

- **登录后台**。用户名是：admin，然后输入刚刚复制的密码。就可以登入后台了。然后打开：**参数设置**、**应用设置**、**本地存储**。
![image.png](/img/hardware/16.webp)

- **常规设置NAS**。设备选择 => NAS => NAS路径:更改 => 手动配置。填入前面服务器的IP。点击链接。然后就会出现你配置好的文件夹。保存就可以了。
![i图片设置NAS.png](/img/hardware/17.webp)
![image.png](/img/hardware/18.webp)


- **到服务器上查看是否有文件写入**
```
ll /usr/nfs-share/
/*  一般等待10~15分钟，视频就会写入服务器了。 
drwxr-xr-x 3 root root 4096 Mar 13 22:11 HN1D012HAJ16081
*/
```
> 我通过上面的方法是无法设置互联网上的nas的，如果你和我一样，那么可以尝试下面的方法。记得必须要在登录的情况下。

- **非常规的设置NAS**。因为我并不是像上面这么顺利的设置的。我在输入服务器的地址之后，**页面并没有任何变化，没有出现服务器共享的文件夹。**查看控制台，监测NFS的请求是200成功了。但是页面就是没有出现共享的文件夹给我设置。所以我想，会不会是摄像头限制了不给连接互联网的nfs?于是，我在自己的电脑上搭建了一个nfs。然后按照上面的步骤，管理界面出现了共享的文件夹，于是我观察了设置nfs的请求。完整的请求如下
```
fetch(
    "http://[监控的IP]/common_page/Internet_TFSD_LocCfg_lua.lua", 
    { 
        "credentials": "include", 
        "headers": 
            { 
                "accept": "application/xml, text/xml, */*; q=0.01", 
                "accept-language": "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7", 
                "content-type": "application/x-www-form-urlencoded; charset=UTF-8", 
                "x-requested-with": "XMLHttpRequest" 
            }, 
        "referrer": "[监控的IP]", 
        "referrerPolicy": "no-referrer-when-downgrade", 
        "body": "IF_ACTION=MountShareDir&isIP=[NFS服务端的IP]&iMntDirName=[NFS的路径]&iProtocol=1&iManFlagID=0&diskUsername=&diskPassword=", 
        "method": "POST", 
        "mode": "cors" 
    }
);
```
需要将以上中括号以及中括号的内容替换。
| 参数          | 说明                                            |
|---------------|-------------------------------------------------|
| 监控的IP      | 就是你监控的地址（内网地址，如：192.168.123.1） |
| NFS服务端的IP | 就是你服务器的地址                              |
| NFS的路径     | 就是前面设置的路径（如：/usr/nfs-share）        |

- **发起请求**。替换上面的参数后，在监控管理界面，（记得刷新一次，防止登录过期。）打开F12，在console面板，复制并执行上面的代码。复制进来，然后回车就好了。
![image.png](/img/hardware/19.webp)

- **前往控制界面，点击保存就可以了**

### 遇到的问题
- **存储的视频无法打开**。摄像头成功存入远程的NFS，而且我对比过本地能够播放的视频的大小，都差不多（20M左右）。但是发现远程的视频并不能打开。目前不知道什么原因，计算了20多M是写入10分钟的，计算下来就是30~50K/s，目前宽带的上行是4M ~ 6M左右，就算没有缓存，按道理实时写入也是没问题的。而且NFS也是基于TCP的，并非UDP，为什么会出错呢？？实在不明白。
![image.png](/img/hardware/20.webp)

![image.png](/img/hardware/21.webp)


### 总结 & 题外话

- **中兴的摄像头不仅可以通过手机APP设置，还能通过网页设置**。之前被中兴的客服工程师坑了，他说必须通过APP而且摄像头必须连接到互联网才能进行设置。其实，说明书的最后一页写着，可以通过网页设置，而且设置更加丰富。
- **中兴摄像头不仅仅可以使用局域网的NAS**，通过上面的方法，可以接入你互联网的服务器。
- **中兴摄像头的地步螺母是标准的3/4螺孔**（就是和相机底部的接三脚架是一样的）。可以到五金店花1块钱能买到好多颗这样规格的螺丝，然后固定就方便了。
> 相信我，我是工程师![image.png](/img/hardware/22.webp)



