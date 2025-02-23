---
title: 【硬件】nodeJs刷新NFS挂载
categories: 硬件
tags: 硬件
date: 2019.03.18 22:43:09
---
> **背景：**因为我在市场装的摄像头的时间错误了，而且是离线的，并且摄像头不能手动设置时间，只能指定一个时间同步的服务器来同步时间。所以，我就想尝试在家里的路由器[能连接互联网]搭建一个的NTP服务器，尝试成功再拿去市场那边同步一下时间。但是，好像路由器的存储不够我存放相关的文件来当做NTP服务器，而且，路由器没有USB接口，不能外挂硬盘或者USB。所以，我想能不能挂载一个NFS上去。

#### 前文
- **本文能帮到你**，如果你的路由器提前root好，而且没有USB存储，并且机身存储不足。或者存储足够，但是不可写入。那么可以挂载额外的存储。
- 主要是实现路由器重启，IP变更，同步修改NFS服务器的配置。从而让NFS更加安全。~~顺便尝试一下nodeJs写服务端~~
- **本文物料说明**：斐讯K2，固件：padavan 3.4.3.9-099_9-2-26（开启SSH）、一台linux服务器（有NFS服务和NodeJs环境）。
#### 思路
> 方案有2种：**NFS**、**SAMBA**。我目前只会搭建和挂载**NFS**，所以我就选择了NFS，而且NFS的性能更加好（然而两者对我没有任何区别）。
- 我在NFS服务器配置了只运行固定的ip能够访问，貌似nfs的权限只能做到这样。但是，我的路由器每天都会定时重启，而重启之后，分配给你的ip就会有变化，原来nfs指定的是旧的ip了。所以重启之后就没办法挂载了。
- 我就想，**能不能再路由器启动完成之后执行一个脚本，通知nfs服务器，我的ip变更了，nfs修改配置，并重启服务。**而nodejs正好能执行脚本，而且非常容易搭建一个http服务。所以就尝试了。流程图如下：
![IMG_20190321_211234-01.jpeg](/img/hardware/11.webp) 



## 方法
> 下面假定**你的nfs服务器地址是：21.22.23.24、nfs共享的目录为/usr/nfs、路由器的公网IP为11.12.13.14**
- 测试连通性，在nfs服务器指定只有你的ip能够挂载，修改**/etc/exports**文件如下：
  ```
  /usr/nfs 11.12.13.14(rw,sync,no_root_squash,no_all_squash)
  ```
  在路由器挂载nfs，在路由器创建一下文件夹**/etc/storage/nfs**（因为路由器重启，所有的文件会被清除，除了**etc/storage**文件夹下的。并且创建完成之后，需要执行**/sbin/mtd_storage.sh save**，这样才能通知路由器，保存在**etc/storage**创建的文件），然后就可以挂载了，执行下面命令
  ```
  mount -o nolock 21.22.23.24:/usr/nfs /etc/storage/nfs
  ```

---
- 下面。搭建一个小型的node服务端，用于我们接受路由器发起的请求，并且验证密码，再替换nfs配置，然后重启nfs服务。这里我们需要2个插件，**express**（用于接受请求）、**shelljs**（用于执行shell）。代码如下，非常简单。[git地址](https://github.com/shihua-guo/node-shell)
```
var express = require('express');
var app = express();
var fs = require('fs');
const { exec } = require('child_process');
const filePath = "/etc/exports";//需要修改的文件
const cmd = "systemctl restart nfs";//需要执行的重启nfs命令
const passwd = "mypwd";//非常简单的密码，用于简单的验证
var extractIpFromString = function(str){//因为接收到可能是ipv6地址，所以我们需要提取ipv4地址
	var r = /\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b/;
	var ip = str.match(r);
	return ip[0];
}
app.get('/',function(req,resp){
    var ip = extractIpFromString(req.ip);//req带有请求的ip，我们只需要把它解析出来
	console.log("将ip替换为：",ip);
    var pwd = req.query.pwd;
    if(ip && pwd === passwd){
        fs.readFile(filePath,'utf-8',function(err,data){
            if (err) {
                resp.send('更换配置失败');
                throw err;
            }
            var newData = data.replace( /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/gi, ip);
            
            fs.writeFile(filePath, newData, 'utf-8', function (err) {
                if (err) {
                    resp.send('更换配置失败');
                    throw err;
                }
              });
        })
    }else{
       resp.end('fuckyou！ ' );//其实就是密码错误，但是别让别人发现了，所以就不发送密码错误了
	return false;
    }

    exec(cmd, (err, stdout, stderr) => {//执行脚本。为了正常运行，所以我们就捕获可能遇到的异常
        console.log("执行重启nfs");
        try{
            if (err) {
                resp.send('重启失败');
            return;
            }
            resp.send('Fuck me！');//这样返回给客户端就是成功了
        }catch(e){
            console.log(e);
        }
	});
});
app.listen(3000);
console.log("app is running on 3000")
```
- 执行，在根目录下，运行下面命令，就可以运行咯！
```
node app
```
  测试结果：这样证明![image.png](/img/hardware/12.webp) 
  同时、我的测试文件的内容就变成了，替换成功，那么我们就可以部署到服务器上面了：
```
/usr/nfs-share 127.0.0.1(rw,sync,no_root_squash,no_all_squash)
```
- **部署服务器，并测试。**使用ftp工具将**package.js**、**app.js**上传到服务器任意一个文件夹。或者使用scp命令。
  ```
  scp -r -p ~/Document/workspace/node-shell root@21.22.23.24:/usr/workspace/
  ```
  然后重复上面的测试过程。然后在服务器执行**npm i** 安装依赖，再执行**node app**，运行  程序。我们可以使用**forever**组件来在后台执行刚刚的nodejs。安装**forever**
  ```
  npm install forever -g 
  forever start app.js  //启动node
  ```
  我们也可以将nodejs添加为开机启动：编辑/etc/rc.d/rc.local文件，直接把命令加入到最下面。然后服务器在重启的时候，就会执行这个命令了。
  然后测试一下，再路由器发起请求，如下：
  ```
  curl "http://21.22.23.24:3000?pwd=mypwd"
  ```
  检查**/etc/exports**文件是否被更改。nfs服务是否重启。你可以先将ip指定到其他ip，确认路由器无法挂载之后，然后进行上面操作，然后再次挂载，如果挂载成功，那么久okay。

- **路由器准备工作**
  - 建立共享的文件夹。由于padavan固件原因，你新建的文件夹都会在路由器重启之后清除掉。所以，共享的文件夹必须要建立在**/etc/storage/**下，建立文件夹还需要**执行保存命令，不然重启之后也会清除掉**。
  ```
  /*建立共享文件夹*/
  mkdir /usr/nfs
  /*保存*/
  /sbin/mtd_storage.sh save
  ```
  或在Padavan后台页面中，在 高级设置>系统管理>“保存 /etc/storage/ 内容到闪存” 点击提交。
  ![image.png](/img/hardware/13.webp) 

- **设置路由器启动脚本**。在**高级设置、自定义、脚本、在路由器启动后执行**，添加**curl "21.22.23.24:3000?pwd=mypwd"**，上面我们写好的脚本。然后在路由器开机之后就会发送该请求，从而改变nfs服务器的指定ip了。

#### 题外话
- **padavan固件**，创建的文件需要在**/etc/storage**文件夹下面建立，并且需要执行**/sbin/mtd_storage.sh save**，之后才会帮你保存，不然重启之后，你创建的文件全部会被清除。
- **因端口未开放，无法挂载**。如果你的服务器配置了安全组，或者服务商没有开启服务器的端口，那么，你需要添加安全组，开放对应的端口。因为nfs是基于rpc的，所以你可以列出rpc服务的相关端口及其协议，然后再配置对应的安全组就可以了。或者，你直接开放所有端口。
   ```
  rpcinfo //这个命令可以查看rpc的相关的。找到nfs字样的，对应的协议和端口，然后再配置安全组、开放对应的协议及端口。
  ```
- **Connection refused**。这个表面意识就是连接拒绝。但是我查看了nfs服务器的配置，是允许了路由器这个ip的。而且没有关闭对应的端口。而且路由器和nfs确实是通的。重启nfs也是没有效果。其他机器连接nfs是没有问题，所有问题估计在路由器上。后来发现是，文件被锁了。mount的时候，需要加上参数**-o nolock**，所以命令就是：
  ```
  mount -o nolock [你有nfs服务器地址，和路由器联通就可以了，无论是局域网还是互联网]:/usr/nfs /etc/storage/nfs
  ```