---
title: '【java】Optional orElseThrow 错误: 未报告的异常错误X; 必须对其进行捕获或声明以便抛出'
categories: 后端
tags: 后端
date: 2020.12.10 17:17:23
---
## 情况说明

以下代码：

```java
Optional.ofNullable(u).map(u->{
	....
}).orElseThrow(()->{
	throw new CommonException("用户不存在！");
});
```

编译的时候抛出异常：

```java
Error:(68, 23) java: 未报告的异常错误java.lang.Throwable; 必须对其进行捕获或声明以便抛出
```

#### 寻找过程

始终没有找到答案，因为我之前一直都是这样用的，```Optional.ofNullable().orElseThrow()```，于是初步怀疑：**IDE有问题**。不过怎么样，就直接搜了。问题可以使用```IDEA Optional Requires Catching Throwable when using orElseThrow method```.找到以下答案：

1. https://stackoverflow.com/questions/39076077/throw-runtimeexception-inside-stream-with-optional-orelsethrow

   从这个答案，发现是JDK的一个异常，同时找到相关的答案：

2. https://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions

   得到了一个解决方案：在```orElseThrow```前面指定一下抛出的泛型：```<XXException>orElseThrow```

## 解决方案

1. 解决方案1：

   **升级JDK**。这个是jdk(1.8.0_92版本前)的一个bug：https://bugs.openjdk.java.net/browse/JDK-8047338。但是，我的jdk版本本来就比这个高，我就升级到最新的版本，问题还是存在！

2. **解决方法2**：

   **在```orElseThrow```前面给泛型指定一下抛出的具体的运行时异常：```<CommonException>orElseThrow```**

   原代码：

   ```JAVA
   Optional.ofNullable(u).map(u->{
   	....
   }).orElseThrow(()->{
   	throw new CommonException("用户不存在！");
   });
   ```

   修改后的代码：

   ```JAVA
   Optional.ofNullable(u).map(u->{
   	....
   }).<CommonException>orElseThrow(()->{
   	throw new CommonException("用户不存在！");
   });
   ```

## 问题分析

1. 这个问题是jdk的一个bug，并且和IDE有关系（eclipse没有这个问题）。我翻译一下老外精彩的分析：

   问题重现，**精简并抽取出报错部分Optional的代码**：

   原来JDK源码：

   ```java
   abstract <T> void f(Supplier<T> s);
   
   abstract <T, X extends Throwable> T g(Supplier<? extends X> x) throws X;
   
   void bug() {
       f(() -> g(() -> new RuntimeException("foo")));
   }
   ```

   **再精简，把lamda除去，这样我们就可以在jdk7编译了，可以对比jdk7和8的差异**，[Gitst文件](https://gist.github.com/shihua-guo/86fa6c1a075568e7860ac2202bb1ea31)：

   ```java
   /**
    * @author guoshihua
    * @description
    * @date 2020/12/10 16:27
    */
   public abstract class Test {
       abstract <T> void f(T t);
   
       abstract <T, X extends Throwable> T g(X x) throws X;
   
       void bug() {
           f(g(new RuntimeException("foo")));
       }
   }
   ```

   使用jdk8u92以前的版本，我这里下载的是77版本，修改环境变量，**把jdk切换到jdk8u77**，并运行javac编译：

   ```bash
   java -version
   java version "1.8.0_77"
   Java(TM) SE Runtime Environment (build 1.8.0_77-b03)
   Java HotSpot(TM) 64-Bit Server VM (build 25.77-b03, mixed mode)
   
   ```

   ```bash
   javac Test.java
   ```

   **成功重现改错误：**

   ```java
   Test.java:14: 错误: 未报告的异常错误X; 必须对其进行捕获或声明以便抛出
           f(g(new RuntimeException("foo")));
              ^
     其中, X,T是类型变量:
       X扩展已在方法 <T,X>g(X)中声明的Throwable
       T扩展已在方法 <T,X>g(X)中声明的Object
   1 个错误
   ```

   把切换到jdk7已经jdk8U92（我这里使用的是jdk8u271）以上的版本编译均正常。

2. 关于Throwable以及其子类，Throwable包含编译的时候异常和运行时异常：

![image.png](/img/java/18.png)


   对于上面的解释：

   ```
   I guess something about passing a generic method return type to a generic method parameter causes the type inference for the exception to fail, so it assumes the type is Throwable and complains that this checked exception type isn't handled. The error disappears if you declare bug() throws Throwable or change the bound to X extends RuntimeException (so it's unchecked).
   ```

   所以，看来源码里面指定的是 Throwable，也包含编译异常，可是可以编译的时候把代码的异常传递给泛型的时候识别错误了，依旧把运行时异常当做了Throwable，所以IDE需要你把他捕获掉。但是，如果你使用泛型指定了运行时异常，编译就通过了。

## 总结

​	这个确实就是JDK的bug，我能力有限，只能重现，无法深究。总结一句就是：1.**升级jdk的版本** 2.**指定抛出的为运行异常**



## 题外话

​	我发现我之前都是这样用的时候并不会报错：

```java
Optional.ofNullable(u).orElseThrow(()->new CommonException("战术异常"));
```
但是，当我使用map的时候，编译的时候IDE就让我捕获异常：

```java
Optional.ofNullable(FmlUser).map(u->{return u;}).orElseThrow(()->{throw new CommonException("用户不存在！");});
```
上面测试的Test.java文件：https://gist.github.com/shihua-guo/86fa6c1a075568e7860ac2202bb1ea31
