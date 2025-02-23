---
title: 【硬件】小米路由器MINI刷潘多拉
categories: 硬件
tags: 小米路由器
date: 2019.02.25 22:02:19
---
#### 小米路由器MINI刷潘多拉教程
##### 刷入开发版，以开启SSH
> 为什么需要刷开发版？因为开发版可以开启SSH连接。而能够访问路由器是一切的基础。路由器其实就是一台linux的电脑。
  1. 直接刷入老版本，新版本是无法开启SSH的。[固件列表](http://www.miui.com/thread-1776173-1-1.html)。直接刷这个版本[**小米路由器mini MiWiFi 开发版 0.8.11**](http://bigota.miwifi.com/xiaoqiang/rom/r1cm/miwifi_r1cm_firmware_426e6_0.8.11.bin)（非常重要，如果版本高了，不能开启SSH，版本低了没有修改root用户的api）。刷入开发版非常简单。
      - 将上面下载好的固件改名为：**miwifi.bin**（非常重要），放入一个U盘的**根目录**（非常重要）。
      - **路由器断电**、**插入U盘**、**拿笔尖按住reset**（非常重要）、**插入电源**。
      - 路由器指示灯先会**常亮黄色**，然后指示灯会**黄色一直闪**（这时候可以松开reset了）、然后静候路由器指示灯变成**蓝色**。就完成刷入开发版的步骤了。**如果指示灯是红色，那么代表这个固件有问题，重新更换固件，断电再走一遍流程就可以了，不用担心会变砖的**
  2. **开启SSH。**~~不用考虑保修了，比较这么旧的机器了~~
 
> 我是参考[这里的](https://www.right.com.cn/forum/forum.php?mod=viewthread&tid=183266&extra=&highlight=%D0%A1%C3%D7%2Bmini%2Bssh&page=1)

  - 刷入开发版之后会正常开机，然后直接把路由器设置成普通模式就可以了（**注意！！如果是中继模式，地址会改变，非常重要！！！地址改变了你就不是通过默认的miwifi.com或者192.168.31.1进入了**），设置于好了，**管理员的密码**需要记下来，后面需要用到（非常重要）
 - **通过接口开启Telnet**，设置好之后，访问路由器的主页。[miwifi.com](miwifi.com)、或者[192.168.31.1](192.168.31.1)，这时候，地址栏是这样的**http://miwifi.com/cgi-bin/luci/;stok=f095a142b2d9a6b246eaa4c2c586fc65)/web/home#router**，把你浏览器中的**/web/home#router** 替换成 **/api/xqnetwork/set_wifi_ap?ssid=tianbao&encryption=NONE&enctype=NONE&channel=1%3B%2Fusr%2Fsbin%2Ftelnetd**、然后等一会浏览器返回：**{"msg":"未能连接到指定WiFi(Probe timeout)","code":1616}**，这样就已经开启了telnet，可以连接路由器了(如果是1617，说明开发版的版本太新了，无法开启)。
 - **通过接口修改root密码**，继续讲上一步，把你浏览器中的**/web/home#router**替换成**/api/xqsystem/set_name_password?oldPwd=当前网页管理密码&newPwd=新的网页管理密码和root密码**，oldPwd就是**你的当前网页管理密码**，newPwd就是**你的新管理密码和root密码**，回车以后网页显示**{"code":0}** 就是成功了。
    >（因为原来是能够通过小米的网站获取路由器的root用户的密码，但是现在关闭了获取入口。为了能够登录路由器，我们只能通过api修改root密码）。
- 通过telnet命令连接：
```
telnet 192.168.31.1 
```
##### 刷入Breed(刷不死)
  - 下载Breed固件。[固件列表](https://breed.hackpascal.net/)，我们需要的 [breed-mt7620-xiaomi-mini.bin](https://breed.hackpascal.net/breed-mt7620-xiaomi-mini.bin) 。
  - 备份原有的uboot。使用telnet登录。输入以下命令：
```
dd if=/dev/mtd1 of=/tmp/xiaomi_uboot.bin
```
系统返回，说明已经将原来的**xiaomi_uboot.bin**备份到**/tmp**：
```
384+0 records in
384+0 records out
``` 

- 使用netcat将**xiaomi_uboot.bin**备份到本地。（因为路由器上缺少非常多工具，比如scp、ftp、sftp等文件传输的工具。所以，我们选择使用nc）
先在自己的电脑上启动一个接收文件的监听:
```
/**意思是我们在（接收的机器）的9995端口建立一个接收文件的流，然后接收到的文件将新建并写入xiaomi_uboot.bin文件*/
nc -l 9995 >xiaomi_uboot.bin
```
在路由器上往电脑的9995端口发送数据，把下面bin包发送过去，记得将10.0.1.162替换成你本地的ip
```
nc 10.0.1.162 9995 < /tmp/xiaomi_uboot.bin
```
- 把breed固件放入/tmp下
在电脑上的固件目录下执行
```
nc -l 9995 <breed-mt7620-xiaomi-mini.bin
```
在路由器上执行，这样就可以把固件传输到路由器上了：
```
nc 10.0.1.162 9992 >/tmp/breed-mt7620-xiaomi-mini.bin
```
- 开始刷入。执行：
```
mtd -r write /tmp/breed-mt7620-xiaomi-mini.bin Bootloader
```
![image.png](/img/hardware/6.webp) 
- 关机。然后接着我们用硬物顶住reset键，插上电源开机，等到灯狂闪的时候，松开reset键，打开浏览器登录192.168.1.1就可以进入Breed的web界面了。
![image.png](/img/hardware/7.webp) 
- 下载潘多拉固件。链接:https://pan.baidu.com/s/1LjNnidBTUNQz4DGRpqleZQ  密码:060a。如果失效，请提醒我。
- 上传固件。**固件更新**、**勾选固件**、**将本地下载好的潘多拉固件上传**
![image.png](/img/hardware/8.webp) 
- 稍等片刻。等待路由器重启，并且亮蓝色的指示灯就可以接入网线进行连接了。注意，以上的潘多拉版本的管理地址是[192.168.1.1](192.168.1.1)。


#### 总结
1. 大概的步骤是：刷入开发版、开启telnet、备份启动文件、刷入Breed不死固件、通过Breed界面刷入潘多拉或者其他固件。
2. 如果无法开启Telnet（报1617），那么大概就是开发版的固件太新。需要更换旧的固件。
3. 路由器上面缺少非常多linux常用的工具。传输文件可以使用netcat传输。
4. 手机访问路由器挂载的硬盘。手机连接上路由器，然后在手机下载一个**ES文件管理器**，在左侧菜单的**NETWORK**的LAN就可以访问到硬盘了。

#### 题外话
> 下面2个关于小米路由器原本固件的问题困扰我很久
1. 小米无法进入管理页面。如果你无法通过192.168.31.1或者miwifi.com进入管理页面，那么可能是你的小米路由器使用了中继模式，中继模式的IP地址是在被中继的设备的域之下。所以你可以连接小米路由器的wifi，然后查看路由器的ip，通过这个ip进入。如图所示：
![image.png](/img/hardware/9.webp) 

2. 小米路由器无法初始化硬盘。提示**请尝试升级路由器和手机客户端再试**。
![image](/img/hardware/10.webp) 

这是因为，你也许重置了路由器（切换了工作模式，IP地址改变了），app绑定的还是重置之前的路由器，但是app仍然显示这个路由器是连接的（其实根本就没有了），而且还提示有存储设备，叫你初始化，但是你一直都没有办法初始化硬盘。