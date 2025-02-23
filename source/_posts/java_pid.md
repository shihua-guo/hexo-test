---
title: 【java】容器被无法获取java进程调试
categories: 后端
tags: 后端
date: 2021-09-30 14:33
---
# 容器被无法获取java进程调试

> 今天尝试在容器内使用arthas调试，发现无法attch到java进程，jstack命令也无法获取到java进程

## 问题描述

1. 使用arthas提示错误

   ```bash
   Unable to get pid of LinuxThreads manager thread
   ```



## 相关问题连接

1. [jmap not happy on alpine · Issue #76 · docker-library/openjdk · GitHub](https://github.com/docker-library/openjdk/issues/76)

   结论：

   If you keep bash as PID 1, you'd no longer get signals from `docker stop` and `docker kill` (without having to write traps). With docker 1.13 you can use `--init` to have docker put in [tini](https://github.com/krallin/tini) as PID 1; it'll forward signals and reap zombies.（自行翻译）

----

2. [(9条消息) docker报错：Unable to get pid of LinuxThreads manager thread及openjdk-alpine镜像无法打印线程堆栈和内存堆栈问题_学亮编程手记-CSDN博客](https://blog.csdn.net/a772304419/article/details/118105136)

   **结论：**

   使用alpine方式打包镜像【FROM openjdk:8-jdk-alpine】，会出现这种情况。通过使用tini插件，可以解决。

   

## 解决方法

1. 修改`Dockerfile`

   添加tini（添加`RUN apk add --no-cache tini`，并且在`ENTRYPOINT`添加`tini`）：

   ```dockerfile
   FROM openjdk:8-jre-alpine
   
   ...
   
   # Install Tini
   RUN apk add --no-cache tini
   
   ENTRYPOINT ["/sbin/tini", "--", "sh", "entrypoint.sh"]
   
   ```
   
   

## 问题分析

> because PID 1 is special and it doesn’t handle any signal unless explicitly declared. So the workaround is to have the java process spawned at non PID 1.

因为启动java的进程ID为1，而PID1是特殊的进程号，不处理任何信号，除非显示声明。所以，我们的解决方案就是：让java进程的ID不为1。而`tini`就是这个作用：`*All Tini does is spawn a single child (Tini is meant to be run in a container), and wait for it to exit. All the while reaping zombies and performing signal forwarding.*`

Tini所做的就是生成一个子节点(Tini是在容器中运行的)，然后等待它退出。进行信号转发。  



## 结论

	学习了新的技能。





