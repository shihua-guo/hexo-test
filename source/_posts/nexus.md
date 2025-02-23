---
title: 【后端】使用nexus建立个人npm库
date: 2017-11-13 23:05:04
categories: 后端
tags: 
---
下面分享一下如何使用nexus建立自己的npm仓库
## 准备工作
------
- java环境
- node环境
- [nexus安装包 3.6.0](https://sonatype-download.global.ssl.fastly.net/nexus/3/nexus-3.6.0-02-win64.zip)

## **运行nexus**
------
进入解压后nexus的bin文件夹，在此目录打开cmd **（使用gitbash执行可能会有问题）**。执行
```
nexus /run
```
默认端口为：[8081](http://localhost:8081/)，打开可以看到nexus界面了。
> 点击右上角可以登录，默认账号密码：admin  admin123

![运行成功的图片](http://owrfhrwdi.bkt.clouddn.com/W4UKA3OGE%5DNO%5D%60NBDQ5%60DVQ.png)
> "箱子"图标就是代表着**仓库中的包**，"齿轮"图标则为**设置**，下面我们进入**设置**

![界面](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171114001115.png)
> 之后，我们将关注**"Repository"**和**"Security"**栏目。分别用于**创建/管理仓库和用户**

## 创建npm需要的Blob存储（此步骤可以忽略）
- 分别创建name为"npm-proxy","npm-hosted","npm-group"的存储（分别用于存放npm代理下载的依赖包、发布的私有依赖包、代理下载的和私有的组合的依赖包）。

![创建存储1](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171123001119.png)

![创建存储2](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171123001258.png)
## 创建npm仓库
------
- 点击**"Create Repository"**
![Create Repository](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171114001556.png)
- nexus增加了许多仓库类型，下面我们只关注和**npm**相关的
![仓库类型](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171114001609.png)

### 下面说下这几种类型的区别
- **hosted** 为私有的仓库，我们在本地写好的npm插件就是发布到这个地方的。这个就是我们搭建私有仓库的目的。
- **proxy** 为代理的镜像地址（我们一般设置为淘宝镜像即可），大概可以理解为，nexus帮我们从这个地址下载其他的npm依赖包。而且会自动缓存到nexus仓库。
- **group** 用于私有仓库和代理仓库的组合。就是我们可以从**group**下载到私有的依赖（存放在nexus仓库的）和npm官网的依赖。
> 需要注意的是，**hosted**仅用于发布你的私有依赖，所以，如果你从**hosted**下载依赖是无法下载的。下载只能通过**group**下载。

### 下面开始建立仓库
1. 创建代理仓库（npm-proxy）
![代理仓库](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171123001544.png)
> - name为**"npm-proxy"**
  - remote storage为**"https://registry.npm.taobao.org"**
  - 存储为**"npm-proxy"**

2. 创建私有仓库（npm-hosted）
![私有类型](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171123001613.png)
> - name为**"npm-hosted"**
  - 存储为**"npm-hosted"**

3. 创建组个仓库（npm-group）
![私有类型](http://owrfhrwdi.bkt.clouddn.com/TIM%E5%9B%BE%E7%89%8720171123001749.png)
> - name为**"npm-group"**
  - 存储为**"npm-group"**
  - 然后在group中，把前面2个创建的拖到右边框中

以上，仓库已经创建完成。可以通过界面进入仓库，查看刚刚创建的仓库。也可以直接访问，如果出现404，则说明name写错咯。地址和name对应的
- http://localhost:8081/repository/npm-proxy/
- http://localhost:8081/repository/npm-hosted/
- http://localhost:8081/repository/npm-group/

### 创建用户
> 之后我们发布需要这个用户登录

![创建用户](http://owrfhrwdi.bkt.clouddn.com/U4WTM%25W%7BSWM68C3IFF%7DF%60QP.png)
同时把**npm Bearer Token Realm**置于active
![创建用户](http://owrfhrwdi.bkt.clouddn.com/M_6BNS%60OHOFCU_HSUURX%28W0.png)

## 测试下载依赖包
1. 切换npm的registry。可以运行一下命令。记住是**npm-group** 这个地址。
```
npm config set registry http://localhost:8081/repository/npm-group/
```

  同样的，我们也可以打开一下这个文件修改设置
```
C:\Users\你的用户名\.npmrc
```
  推荐设置（**可以解决phantomjs、chromedriver、node-sass无法下载的问题**）。直接把一下拷贝进.npmrc文件即可
  ```
  loglevel=info
  scripts-prepend-node-path=true
  registry=http://localhost:8081/repository/npm-group/
  chromedriver_cdnurl=http://cdn.npm.taobao.org/dist/chromedriver
  phantomjs_cdnurl=http://cnpmjs.org/downloads
  sass_binary_site=https://npm.taobao.org/mirrors/node-sass/
  ```
2. 设置完之后，我们可以随便下载一个依赖，看是否是经过nexus下载的。
  ```
  npm install -g generator-vuejs
  ```
  可以看到的确是走npm-group仓库的。
  ![测试](http://owrfhrwdi.bkt.clouddn.com/VZWA5%25L9U2%250X%29I$1%7BV%5DHSL.png)
  这时候去nexus库查看，可以看到，nexus把从淘宝下载的镜像都缓存在本地了。
  ![缓存](http://owrfhrwdi.bkt.clouddn.com/4RVO~K5PROHTXU%7B$125@_YE.png)

## 测试发布
1. 切换成**npm-hosted** 的地址
  ```
  npm config set registry http://localhost:8081/repository/npm-hosted
  ```
2. 登录，运行以下命令，然后输入刚刚创建的用户名和密码即可
  ```
  npm adduser 
  ```

  ![登录](http://owrfhrwdi.bkt.clouddn.com/TSK0$V7J3H_F_L6LBO32YLL.png)

3. 找一个需要发布的依赖，在根目录运行以下命令，设置registry为npm-hosted，并发布
  ```
  npm publish
  ```
  200即为发布成功
  ![登录](http://owrfhrwdi.bkt.clouddn.com/MUYXX3X%5B9JMD4F$~EM4P%28$1.png)
  去nexus仓库可以看到刚刚发布的依赖包
  ![登录](http://owrfhrwdi.bkt.clouddn.com/N%5DT%29Q4$%7B%5DZ0%28WF%28%25OE%7BR%257U.png)
