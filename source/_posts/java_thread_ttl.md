---
title: 【java】InheritableThreadLocal NullPointException & 线程池环境下使用TTL进行线程上下文传递
categories: 后端
tags: 后端
date: 2022-04-01 09:25
---
#  InheritableThreadLocal NullPointException & 线程池环境下使用TTL进行线程上下文传递

> 背景：因为业务需要，在某个接口处理超过3秒，就即时返回。因此我使用了Future的 超时特性。然后我又用线程池去处理Future任务。同时我之前又加了一个切面，那么切面有一个InheritableThreadLocal变量，用于存放请求上下文信息。原来是ThreadLocal，但是子线程也需要用到，所以切换到了InheritableThreadLocal。就是这个InheritableThreadLocal在线程池中导致了NPE问题

关键词：InheritableThreadLocal 空指针异常 Future NPE

## 原因

InheritableThreadLocal 无法在线程池中获取到父线程的信息。我这里，Future 是在线程池中执行的，所以导致了InheritableThreadLocal  抛出 NullPointException

## 解决方案

> 先说解决方案

1. 不使用线程池，每次执行都new 一个线程。

2. 使用阿里的**[ transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)**

   1. 引入maven

      ```xml
      <dependency>
          <groupId>com.alibaba</groupId>
          <artifactId>transmittable-thread-local</artifactId>
          <version>2.12.4</version>
      </dependency>
      ```

   2. 修饰**Runnable**和**Callable**

      ##### Runnable

      ```JAVA
      // 全局变量
      TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>(); // 使用TTL传递
      /*------------------在父线程中设置---------------------*/
      context.set("value-set-in-parent");
      /*------------------在父线程中设置---------------------*/
          
      
      /*------------------额外的处理---------------------*/
      Runnable task = new RunnableTask();
      // 额外的处理，生成修饰了的对象ttlRunnable
      Runnable ttlRunnable = TtlRunnable.get(task);
      executorService.submit(ttlRunnable);
      /*------------------额外的处理---------------------*/
      
      
      /*------------------在子线程中使用---------------------*/
      // Task中可以读取，值是"value-set-in-parent"
      String value = context.get();
      /*------------------在子线程中使用---------------------*/
      
      ```

      ##### Callable

      ```JAVA
      // 全局变量
      TransmittableThreadLocal<String> context = new TransmittableThreadLocal<>(); // 使用TTL传递
      /*------------------在父线程中设置---------------------*/
      context.set("value-set-in-parent");
      /*------------------在父线程中设置---------------------*/
          
      
      /*------------------额外的处理---------------------*/
      Callable call = new CallableTask();
      // 额外的处理，生成修饰了的对象ttlRunnable
      Callable ttlCallable = TtlCallable.get(call);
      executorService.submit(ttlCallable);
      /*------------------额外的处理---------------------*/
      
      
      /*------------------在子线程中使用---------------------*/
      // Task中可以读取，值是"value-set-in-parent"
      String value = context.get();
      /*------------------在子线程中使用---------------------*/
      
      ```



# 分析

> 我喜欢刨根问底，没有得到本质，到时候还是会忘记的。这里只分析通信过程，忽略保证线程安全等细节东西。这样不会影响主流程

### 线程之间如何通过InheritableThreadLocal  & ThreadLocal  通信？

 看源码可以看到，**ThreadLocal**  **本质**只是操作 **Thread** 类的 **threadLocals** 成员变量的一个工具而已。

 Thread的对2个成员变量的说明：

```JAVA
/* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
ThreadLocal.ThreadLocalMap threadLocals = null;

/*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
```

  而 **InheritableThreadLocal**  继承了 **ThreadLocal**  ，和父类**ThreadLocal**  本质没有什么区别，只是变成了操作 **Thread**类 的 **inheritableThreadLocals** 成员变量。

  ThreadLocal对应代码：

```JAVA
public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}

// 初始化map的方法 get 或者 set的时候如果补存在就会初始化
void createMap(Thread t, T firstValue) {
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
 
```

  **InheritableThreadLocal**重写了父类**ThreadLocal**的 **createMap** 和  **getMap** 方法，从而在初始化和赋值的时候，变成了操作 **inheritableThreadLocals**

  InheritableThreadLocal 对应的代码：

```java
// 仅仅是重写了ThreadLocal的getMap和createMap方法
ThreadLocalMap getMap(Thread t) {
	return t.inheritableThreadLocals;
}

void createMap(Thread t, T firstValue) {
    t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
}
```



### 大致流程

> 因为时间有限，我只看了主流程，细枝末节还有细节得东西我都忽略了，并且是我个人得总结，可能有不少纰漏，仅供参考

> 我推测ThreadLocals和inheritableThreadLocals 是再Thread类出现之后才加的。看注释也验证了我这一点。因为我觉得这样设计有点破坏Thread类了。怎么说？**InheritableThreadLocal 和 ThreadLocal** 操作的 2个变量都是Thread类里面的，InheritableThreadLocal 和 ThreadLocal 本质只是操作那2个成员变量的工具而已。

- ThreadLocal

  ThreadLocal 比较简单，就是 set的时候，获取到当前线程的 threadLocals 变量，然后存取数据

- InheritableThreadLocal 

  InheritableThreadLocal 仅仅是在线程初始化的时候，继承了一下父线程的 **inheritableThreadLocals** 变量，从而达到父子线程传递的目的。

  大致流程如下：

  ```
  void createMap(Thread t, T firstValue) {
      t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
  }
  ```
![博客-ThreadLocal 理解.png](/img/java/3.png)


### inheritableThreadLocals 如何 做到在父线程和子线程直接传递？

在Thread 创建的时候，就把**父线程的inheritableThreadLocals 赋值到当前线程**了。

关键代码(Thread.java 的 418行)：

```JAVA
if (inheritThreadLocals && parent.inheritableThreadLocals != null)
    this.inheritableThreadLocals =
        ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
```



### 为什么线程池中读取不到？

线程池的线程已经脱离了外层线程的体系，没有什么关联。但是我不知道为什么，，并发量非常小得情况，，可以读取到。...。。。我之后有时间再更新吧



### 疑问

目前发现在并发量不高的情况下，线程池会复用原有线程。但是并发一上去，线程池的线程就都是全新的，和原来的没有关系





# 总结

我之后有时间再更新吧
