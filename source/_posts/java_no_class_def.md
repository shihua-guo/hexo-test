---
title: 【java】记录一次 java.lang.NoClassDefFoundError 异常
categories: 后端
tags: 后端
date: 2019.07.14 14:49:43

---
### 背景
> 在调用第三方jar包的时候抛出的一个异常，提示找不到一个类：
```java
org.springframework.web.util.NestedServletException: Handler processing failed; 
nested exception is java.lang.NoClassDefFoundError: 
Could not initialize class com.obs.services.internal.Constants
```
### 查找bug过程
- 查看maven install的日志，没有编译的错误。
- 在本地IDE运行工程，查找对应的 **Constants.class** 文件，存在√。
- 查找本地IDE编译的target文件夹中的lib文件， 对应jar包的**Constants.class** 文件，存在√。
- 查找打包之后的lib文件， 对应jar包的**Constants.class** 文件，存在√。
- 更换jdk版本（1.7 -> 1.8），问题依旧。
- 截获异常。catch Throwable级别的异常，没有多大帮助，提示信息还是一样**NoClassDefFoundError**，但是**Constants**这个类确实是存在的。
### 如何寻找到问题所在
- 我看了**Constants**类都是一些静态变量，想能不能复制出来，然后直接main函数获取里面的值（调用）该类，使得该类能够初始化。
- 复制出来运行确实就是**Constants**类依赖的另外一个类，另外一个类再依赖第三方的类，**然后真正缺少的其实是这个第三方的类。**
- **最后把缺少的第三方的jar包在pom文件补上就可以了**

### 分析原因
造成**NoClassDefFoundError**的原因有好几种。
  - **jar包冲突**。运行环境的classPath中的有多个一样的包（新旧版本共存）。A类调用了B类旧版本的一个方法，但是B类新版本并不存在这个方法。[相关连接]([https://segmentfault.com/a/1190000014938685](https://segmentfault.com/a/1190000014938685)
)
  - **编译jar包/类存在，运行时jar包/类被删除或者改名**。
  ![image.png](/img/java/19.png)
  我们遇到的就是这种情况。估计**是对 A 进行打包的时候，其依赖的B包同时存在于项目中，但是把A.jar给我们用的时候，并没有告知我们A.jar还依赖B.jar，导致我们本地缺少B.jar。但是编译的时候貌似并没有对A.jar进行检查，等到运行时候，才发生的初始化失败。**我写了一个例子:[https://github.com/shihua-guo/Java-Learn/tree/master/NoClassDefFoundError/src/main/java/com/binana/noClassDefineDemo1](https://github.com/shihua-guo/Java-Learn/tree/master/NoClassDefFoundError/src/main/java/com/binana/noClassDefineDemo1)


  - **被依赖的类初始化失败**。一个类被编译的时候，不会出现错误。但是当运行时，该类被调用，然后该类就会进行初始化，但是由于各种原因，这个类初始化失败。我写了一个例子:[https://github.com/shihua-guo/Java-Learn/tree/master/NoClassDefFoundError/src/main/java/com/binana/noClassDefineDemo](https://github.com/shihua-guo/Java-Learn/tree/master/NoClassDefFoundError/src/main/java/com/binana/noClassDefineDemo)

    > **第一次调用该类，但是由于初始化的时候，出现了异常，所以抛出ExceptionInInitializerError。第二次调用该类，但是！因为之前初始化失败，JVM会放弃初始化，而抛出另外一个异常：NoClassDefFoundError**

#### 总结
- 编译能正常，但是运行不一定正常。
- 熟悉类加载过程，发生这种异常，很大可能是在静态代码块处有异常，导致类初始化失败（ExceptionInInitializerError）。然后jvm不会继续初始化该类。尽管该类的class文件存在，但是在运行的时候，初始化失败，也会抛出**NoClassDefFoundError**。