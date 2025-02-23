---
title: 【java】java应用无法访问-访问超时-日志打印正常
categories: 后端
tags: 后端
date: 2021-05-15 14:36:02
---
# java应用无法访问-访问超时-日志打印正常



## 背景

最近一个应用出现了一个奇怪的现象，持续了几个月了。但是一直没排查出来是什么问题。表现如下：程序运行一段时间之后，接口无法访问（访问超时，一直没有到达后台），但是日志没有报错信息。



## 结果

先说下出现以上问题的原因，我们使用的是undertow容器，而非tomcat，因为有一个请求处理过程需要访问到其他应用的接口，但是该应用接口响应非常非常慢，导致过多的线程卡住了，新的请求没法被处理。最终结果就是调整容器的最大线程数。在应用配置文件添加如下配置：

```YML
server:
  undertow:
    io-threads: 8
    worker-threads: 400
```



## 排查思路

1. **检查该问题出现在那一个层级**

   >  该应用是经典的：nginx+docker运行。

   直接通过地址访问（nginx层），结果超时。查看

   | 层级                                      | 结果 | 处理结果                                                     |
   | ----------------------------------------- | ---- | ------------------------------------------------------------ |
   | **直接通过地址访问（nginx层）**           | 超时 | 查看nginx日志，正常，暂时**排除nginx问题**。但是应用日志没有打印。 |
   | **进入宿主机直接访问应用**                | 超时 | 查看应用日志，没有相关日志打印。继续排查                     |
   | **进入docker容器直接用localhost访问应用** |      | 查看应用日志，没有相关日志打印。可以确定是应用出了问题       |

2. **目前已经定位到了应用或者应用的机器问题**，继续排查

   我们可以先检查一下应用服务器的各项指标是否正常

   | 指标             | 状态 |
   | ---------------- | ---- |
   | CPU              | 正常 |
   | 内存             | 正常 |
   | 应用宿主机磁盘IO | 正常 |

   以上指标正常，排除服务器问题。下面因为应用的日志是正常的，所以没办法通过日志判断问题点。

3. 下面继续排查**应用的一些指标**

   我们可以通过arthas去协助我们更快的找到问题所在，可以查看我之前写的，如何在docker容器下使用arthas

   [docker使用atrhas](https://www.jianshu.com/p/c43606d0de74)

   | 指标          | 状态                                                         |
   | ------------- | ------------------------------------------------------------ |
   | jvm的内存情况 | 异常（survival区只有18M，并且使用率100%）                    |
   | 线程状况      | 可能异常（目前线程数200多左右，考虑到tomcat默认是200，所以可能是线程满了） |

   **jvm的内存情况异常解决**

   下面是一些细节，可以看到，survival区只有18M貌似有点问题，所以我们还是解决一下：

   ![image-20210515105226190.png](https://upload-images.jianshu.io/upload_images/2070425-a7f4ec7992fc0c24.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


   说明：由于使用的是JDK8，默认开启 AdaptiveSizePolicy

   AdaptiveSizePolicy(自适应大小策略) 是 JVM GC Ergonomics(自适应调节策略) 的一部分。

   如果开启 AdaptiveSizePolicy，则每次 GC 后会重新计算 Eden、From 和 To 区的大小，计算依据是 GC 过程中统计的 GC 时间、吞吐量、内存占用量。

   JDK 1.8 默认使用 UseParallelGC 垃圾回收器，该垃圾回收器默认启动了 AdaptiveSizePolicy。所以我的解决方案：把**AdaptiveSizePolicy关闭**了

   下面是参考文章：https://blog.csdn.net/weixin_47083537/article/details/106788596

   解决方案：在启动的时候添加参数```-XX:+UseConcMarkSweepGC```

   ```java -jar -XX:+UseConcMarkSweepGC /app.jar```

   DOCKERFILE如下：

   ```JSON
   FROM java:8
   
   ENV TZ=PRC
   RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
   
   ADD target/lean-api-*.jar /app.jar
   
   ENTRYPOINT ["sh","-c","java -jar -XX:+UseConcMarkSweepGC /app.jar $PARAMS"]
   ```

   

   #### 线程问题处理

   > 目前线程数200多左右，考虑到tomcat默认是200，所以可能是线程满了

   下面是arthas线程状况

   ![image-20210515112822709.png](https://upload-images.jianshu.io/upload_images/2070425-fbbe5b78280e8837.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


   由于习惯，我第一反应就是调整tomcat的最大线程数

   ```YML
   server:
     tomcat:
       max-threads: 800
   ```

   重启，继续观察。发现应用还是挂了。。。。下面就百思不得其解了。进入容器，继续启动arthas观察，线程数依旧维持在200个左右，所以可能是其他问题了。

   #### 转折

   在朋友建议下，我把线程栈给dump出来，

   进入容器：

   ```bash
   # docker 容器
   docker ps # 获取docker 运行的容器列表
   docker exec -it xxxx bash  # 进入对应的容器
   
   # k8 容器
   kubectl get pods -n xxx # 查看k8 xx的namespace下面的pods
   kubectl exec -it xxx bash -n xxx # 进入容器
   
   ```

   查找对应的java进程号：

   ```bash
   ps -ef | grep java
   ```

   打印线程栈到文件：

   ```bash
   jstack 8 > /tmp/20210515.txt # 8 是找到的java进程号，把该java应用的线程栈输出到/tmp/20210515.txt文件中
   ```

   把容器的文件拷贝到宿主机中，方便后续分析（非必须）

   ```bash
   # docker 
   docker cp xxxx:/tmp/20210515.txt /tmp/20210515.txt 
   
   # k8s
   kubectl cp xxxx:/tmp/20210515.txt /tmp/20210515.txt -n xxx # 把对应命名空间下的指定容器的文件：/tmp/20210515.txt拷贝到宿主机上
   
   ```

   分析线程栈：发现并没有tomcat线程，经过询问才发现，我们的springboot 应用并没有使用tomcat容器，而是undertow。这就解释了，为什么前面调整tomcat的线程数不起作用。搜索该线程栈文件，发现有一个方法开了非常多线程，有128个

   ![image-20210515142028183.png](https://upload-images.jianshu.io/upload_images/2070425-7b77af42e020bd8b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


   我们再看下undertow的默认配置：

   ![image-20210515142110194.png](https://upload-images.jianshu.io/upload_images/2070425-216ed52e75e155e4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

   该机器是16核心，所以最大工作线程数是核心数的8倍，正好是128个工作线程。同时在postman调用了该外部应用接口，响应时间超过了30s。

   所以，基本可以定位到该问题了：**请求超过了undertow的最大工作线程，并且是被queryUserInfo方法卡住的**



### 优化思路

> 问题找到了，下面就是解决问题。主要分2块，1是调整系统参数，2是调整业务代码以减少请求时间

1. 调高undertow的最大工作线程（治标不治本，只能延缓该问题的出现时间），方法：

   ```YML
   server:
     undertow:
       io-threads: 8
       worker-threads: 400 # 从原来的128调高至400
   ```

2. 给该外部接口增加缓存（因为被调用方的该接口属于非常少变动的，可以放心加长时间的缓存）

   使用内存缓存或者redis都可以，过期时间看具体需求。

   

# 总结

1. 这个问题困扰了我们很久，陆陆续续花了不少时间找，但是方向都是错的。最后还是请教了大神给了思路和方向
2. 不要放弃，多去了解java的基础知识。对于这种不太常见的问题，还是需要对系统全面的了解，并且排查真的需要一定的经验。

