---
title: 【数据库】人大金仓kingbase invalid value for parameter client_encoding ANSI_X3.4-1968
categories: 数据库
tags: 数据库
date:  2021.04.14 16:52
---
# 人大金仓kingbase invalid value for parameter client_encoding ANSI_X3.4-1968



## 背景

应用连接数据库的时候提示以下错误：

```java
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'org.springframework.boot.autoconfigure.jdbc.DataSourceInitializerInvoker': Invocation of init method failed; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'dataSource': Invocation of init method failed; nested exception is com.kingbase8.util.KSQLException: ������������: ������ "client_encoding" ������������: "ANSI_X3.4-1968"
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1796)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:595)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:517)
	at org.springframework.beans.factory.support.AbstractBeanFactory.lambda$doGetBean$0(AbstractBeanFactory.java:323)
	at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:226)
	at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:321)
	at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:202)
	at org.springframework.cloud.autoconfigure.RefreshAutoConfiguration$JpaInvokerConfiguration.init(RefreshAutoConfiguration.java:120)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleElement.invoke(InitDestroyAnnotationBeanPostProcessor.java:389)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor$LifecycleMetadata.invokeInitMethods(InitDestroyAnnotationBeanPostProcessor.java:333)
	at org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor.postProcessBeforeInitialization(InitDestroyAnnotationBeanPostProcessor.java:157)
	... 26 common frames omitted

```



## 怀疑问题

1. **~~jdbc驱动连接没有设置和数据库一致的编码~~**

   人大金仓的资料实在是少，甚至如何在jdbc连接设置编码都无法找到，但是我们可以查找kingbase://协议，看看别人是怎么写的，发现是这样写的：

   ```*jdbc:kingbase8*://***:5419/cdb?useUnicode=true&characterEncoding=utf-8```。于是改上去，并没有用。排除改方向

2. **~~Druid不支持kingbase8~~**

   在百度确实找到了这个说法，但是官网上不去，没办法验证这个说法。而且本地可以正常连接，服务器连接不正常。排除改方向



## 解决步骤

1. **解决初始化连接返回的乱码信息**

   ```bash
   com.kingbase8.util.KSQLException: ������������: ������ "client_encoding" ������������: "ANSI_X3.4-1968"
   ```

   在以上信息可以看到，这个应该是链接的时候返回的，第一反应就是去查看kingbase日志：```/data/kingbase/ES/V8/data/sys_log```。可以看到如下信息：

   ```bash
   2021-04-14 16:18:43 CST 致命错误:  参数 "client_encoding" 的值无效: "ANSI_X3.4-1968"
   ```

   终于发现错误本体了，但是，是什么导致了乱码？乱码肯定是中文不是英文了，所以应该有什么地址可以设置日志的编码。不过kingbase基本和postgresql一致，所以按照postgresql的方法修改日志的编码即可。大致找了下kingbase的日志，发现在这个文件```kingbase.conf```

   **修改kingbase.conf日志编码**

   ```bash
   lc_messages = 'zh_CN.UTF-8'
   ```

   修改成：

   ```bash
   lc_messages = 'en_US.UTF-8'
   ```

   重启kingbase：```sudo systemctl restart kingbase```

   之后可以看到java的日志打印正常了：

   ```FATAL: invalid value for parameter "client_encoding": "ANSI_X3.4-1968"```

2. **调试源码**

   >  根据提示信息查找，发现并没有查看到有用的信息，所以尝试调试源码

   在以下文件的对应方法[```com.kingbase8.core.v3.ConnectionFactoryImpl#openConnectionImpl```]找到了108行关键代码：

   ```JAVA
   newStream = new KBStream(socketFactory, hostSpec, connectTimeout);
   String client_encoding;
   if (KBProperty.CLIENT_ENCODING.get(info) == null) {
   client_encoding = System.getProperty("file.encoding");
   info.setProperty("clientEncoding", client_encoding);
   LOGGER.log(Level.FINE, "Use current JVM default encoding {0}", client_encoding);
   }
   
   client_encoding = KBProperty.CLIENT_ENCODING.get(info);
   newStream.setEncoding(Encoding.getJVMEncoding(client_encoding));
   ```

   可以看到初始化连接的时候，获取了jvm_encoding。这里，我们可以调试一下，在容器下的jvm_encoding究竟是什么编码？

   可以使用arthas修改容器class文件打印以下，或者随便在启动的时候日志打印以下System.getProperty("file.encoding")。容器只记录日志的输出，所以可能使用System.out.println会无法显示。

3. **修改docker的jvm_encoding**

   在dockerfile添加以下代码即可：```ENV JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8```

   完整的Dockerfile大致如下：

   ```dockerfile
   #FROM指令必须指定且需要在Dockerfile其他指令的前面,指定的基础image可以是官方远程仓库中的，也可以位于本地仓库
   FROM livingobjects/jre8
   # UTF-8 并配置环境
   ENV LANG C.UTF-8
   ENV JAVA_TOOL_OPTIONS -Dfile.encoding=UTF8
   #使容器中的一个目录具有持久化存储数据的功能，该目录可以被容器本身使用
   VOLUME /tmp
   #从src目录复制文件到容器的dest。其中src可以是Dockerfile所在目录的相对路径，也可以是一个URL，还可以是一个压缩包
   ADD target/*.jar app.jar
   RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
   RUN echo 'Asia/Shanghai' >/etc/timezone
   #ADD wait-for-it.sh /wait-for-it.sh
   RUN bash -c 'touch /app.jar'
   #指定Docker容器启动时执行的命令，可以多次设置，但是只有最后一个有效。
   ENTRYPOINT ["java","-Dfile.encoding=UTF8","-jar","/app.jar","--spring.profiles.active=test","&"]
   ```

4. 修改重新build了之后可以发现，连接正常了。



## 总结

1. 网上关于人大金仓的资料比较少，如果遇到问题，可以尝试把关键字更换成postgresql再查看，也许更容易查找到答案
2. 需要意识到容器化部署下一些参数不一致。
3. 网上找不到答案可以从源码入手

