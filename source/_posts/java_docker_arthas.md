---
title: 【后端】docker使用atrhas
categories: 后端
tags: 后端
date: 2020.12.24 10:02:57
---
# 简介

Arthas 是一款Alibaba开源的Java诊断工具，可以直接热更新代码，无需重启应用。这个对于我们排查问题十分的方便。官方有非常好的教程，提供临时服务器，在线执行命令可以很方便快速的入门：https://arthas.aliyun.com/doc/arthas-tutorials.html?language=cn

>有时候，我们在测试环境需要打印一些日志，但是代码里面并没有输出。所以，以往我们会在本地添加对应日志之后，再推送到git，然后再到Jenkins构建，这样会比较麻烦。我们可以通过**arthas直接添加需要的日志进行监控**。

下面说下在docker容器下如何使用。

# 安装

1. 找到对应的镜像id

   ```bash
   sudo docker ps
   ```

2. 进入对应镜像id的docker容器：

   ```bash
   docker exec -it [容器ID]  bash
   ```

3. 下载Arthas的jar包 

   ```bash
   curl -O https://arthas.aliyun.com/arthas-boot.jar
   ```

   下载完成，可以查看到当前目录下已经下载好了arthas的jar包，我们就可以直接运行该jar包：```arthas-boot.jar```

4. 运行Arthas，并选择监控的java进程

   ```bash
   java -jar arthas-boot.jar
   ```

   出现如下提示：

   ```bash
   root@localhost:/usr# java -jar arthas-boot.jar
   [INFO] arthas-boot version: 3.4.5
   [INFO] Found existing java process, please choose one and input the serial number of the process, eg : 1. Then hit ENTER.
   * [1]: 6 /demo.jar
   ```

   并选择对应的java进程序号，比如需要监控demo应用，那么就输入：1。选择后，arthas完成启动，并打印以下信息：

   ```bash
   [INFO] arthas home: /root/.arthas/lib/3.4.5/arthas
   [INFO] Try to attach process 6
   [INFO] Attach process 6 success.
   [INFO] arthas-client connect 127.0.0.1 3658
     ,---.  ,------. ,--------.,--.  ,--.  ,---.   ,---.                           
    /  O  \ |  .--. ''--.  .--'|  '--'  | /  O  \ '   .-'                          
   |  .-.  ||  '--'.'   |  |   |  .--.  ||  .-.  |`.  `-.                          
   |  | |  ||  |\  \    |  |   |  |  |  ||  | |  |.-'    |                         
   `--' `--'`--' '--'   `--'   `--'  `--'`--' `--'`-----'                                                                      
   ```



# 修改java文件

>有时候，我们在测试环境需要打印一些日志，但是代码里面并没有输出。所以，以往我们会在本地添加对应日志之后，再推送到git，然后再到Jenkins构建，这样会比较麻烦。我们可以通过arthas直接添加需要的日志进行监控

1. **找到对应的类并反编译**。

   比如下面我们需要在```UserController```这个类添加一些日志，那么需要找到对应的包路经然后，反编译到docker容器内的```/tmp```文件夹下	

   ```bash
   jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java
   ```

   执行成功后，对应的java文件就会出现在docker容器的```/tmp```目录下了。这时候**，我们需要另开一个会话窗口在容器内进行操作**。

2. **编辑对应的类**。

   >  因为这个类我们是通过反编译出来的，所以代码上会有一些优化，**可读性不如我们的工程里面的代码**。我们也可以直接拿工程里面的代码替换上去。但是反编译的话比较方便点。

   使用vi/vim命令编辑

   ```bash
   vim /tmp/UserController.java
   ```

   如果提示vim编辑器没有安装，我们也可以把代码拷贝到容器外面进行修改：

   我们返回到或者新建一个服务器的回话窗口，并把对应容器id的对应java文件拷贝到当前用户目录下：

   ```bash
   sudo docker cp [容器id]:/tmp/UserController.java ~
   
   # 编辑文件
   vi ~/UserController.java
   ```

   修改完成后，再把文件拷贝回容器内：

   ```bash
   sudo docker cp ~/UserController.java [容器ID]:/tmp/UserController.java
   ```

3. **寻找对应的类加载器**【我们后面编译的时候需要用到】

   执行命令，查看对应类的类加载器的hash

   ```bash
   sc -d *UserController | grep classLoaderHash
   ```

   列出所有类加载器的hash

   ```bash
   classloader -l 
   
   #############################显示如下#####################
   name															loadedCount   hash
   org.springframework.boot.loader.LaunchedURLClassLoader@31221be2  18083        31221be2  sun.misc.Launcher$AppClassLoader@7f31245a                                       
    sun.misc.Launcher$AppClassLoader@7f31245a                        47           7f31245a  sun.misc.Launcher$ExtClassLoader@3ba9ad43                                       
    sun.misc.Launcher$ExtClassLoader@3ba9ad43                        41           3ba9ad43                     
   ```

   可以看到对应的类加载器就是：LaunchedURLClassLoader

4. **通过mc命令进行内存编译**

   ```bash
   mc --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader /tmp/UserController.java -d /tmp
   ```

   执行后会输出编译好的class文件位置：```Memory compiler output: **/tmp/com/example/demo/arthas/user/UserController.class** Affect(row-cnt:1) cost in 346 ms```

5. **再使用redefine 重新加载对应的class文件**

   ```bash
   redefine /tmp/com/example/demo/arthas/user/UserController.class
   ```

   完成，接下来就正常的执行原逻辑代码吧，如果没有提示编译错误，就可以看到添加的代码了。

   

# 总结

Arthas 简化了我们排查问题的步骤。





