---
title: 【java】JDK Client & Server 变量可见性问题
categories: 后端
tags: 后端
date: 2022-04-25 14:37
---
# JDK Client & Server 变量可见性问题	

> 下面主要是引出不同jdk对成员变量的处理

### 背景

一天在群里吹逼的时候，有位老哥发了一段代码出来，他说他运行的结果和网上的不一致。然后我们分别在各自的环境运行了，我们的结果和网上的运行一致。代码如下（你们可以拷贝到自己环境测试一下，大概率都是第二种结果）：

```JAVA
public class VisibilityTest {

    private boolean flag = true;

    private int i = 0;

    public void refresh() {
        flag = false;
        System.out.println(Thread.currentThread().getName() + "修改flag:"+flag);
    }

    public void load() {
        System.out.println(Thread.currentThread().getName() + "开始执行.....");
        System.out.println(Thread.currentThread().getName() + "修改flag:"+flag +",i:"+i);
        while (flag) {
            i++;
            //TODO 业务逻辑
        }
        System.out.println(Thread.currentThread().getName() + "跳出循环: i=" + i);
    }
    public static void main(String[] args) throws InterruptedException {
        VisibilityTest test = new VisibilityTest();
        Thread threadA = new Thread(() -> test.load(), "ThreadA");
        threadA.start();
        Thread.sleep(100);

        Thread threadB = new Thread(() -> test.refresh(), "ThreadB");
        threadB.start();
    }
}
```

那位老哥运行结果如下（**会跳出循环，程序马上停止运行**）：

```
ThreadA开始执行.....
ThreadA修改flag:true,i:0
ThreadB修改flag:false
ThreadA跳出循环: i=93402143

```

我们正常人运行的结果如下（**不会跳出循环，程序一直执行**）：

```
ThreadA开始执行.....
ThreadA修改flag:true,i:0
ThreadB修改flag:false

```



### 分析问题

对于这种问题，第一反应肯定就是环境原因，因为代码都是一致的。那么究竟是哪一个不一致导致的呢？

1. ~~对比jdk版本~~。发现都是jdk1.8，排除了。。这就奇怪了，究竟还有什么是不一样的呢？？

我叫那位老哥敲一下：`java -version`试试：

![image.png](/img/java/3.png)

然后，另外一个老哥发现了端倪。

![image (1).png](/img/java/4.png)


我们的都是**Server** 版本，那位老哥的是**Client**版本。

然后我就拿老哥的Client版本试了下，然后运行结果和他的一致了。



### 破案--JDK Client & Server 模式下对变量不同的处理

参考连接：

[jvm - Real differences between "java -server" and "java -client"? - Stack Overflow](https://stackoverflow.com/questions/198577/real-differences-between-java-server-and-java-client/35913837#35913837)

翻译：

调试提示:对于服务器应用程序，请确保在调用JVM时始终指定-server JVM命令行开关，即使在开发和测试时也是如此。 服务器JVM比客户机JVM执行更多的优化，比如从循环中提升那些在循环中没有修改的变量; 可能在开发环境(客户机JVM)中工作的代码可能在部署环境(服务器JVM)中中断。 例如，如果我们在清单3.4中“忘记”将睡着的变量声明为volatile，那么服务器JVM可以将测试提升出循环(将其变成一个无限循环)，但客户机JVM不会。 在开发中出现的无限循环比只在生产中出现的无限循环的成本要低得多。  

```JAVA
volatile boolean asleep;
...
while (!asleep)
   countSomeSheep();
```



##### 个人见解

我认为，Server模式比Client 的变量做了更多性能的优化，比如这个成员变量。Server为了更高的性能，貌似不会保证成员变量在多线程环境下的一致性，需要指明：`volatile`才能保证多线程环境下的一致。（**多线程在不同的核心进行计算，变量是在寄存器中参与运算的，不同核心运算(多线程下)之间的运算会有变量的副本，这就导致了不同线程实际上操作的变量不是同一份，这就是线程不安全**，但是把寄存器的变量刷回主存，同时还需要对其他线程加锁才行，性能比较低，所以Server就默认采用了"牺牲一致性，保证性能"的方案把，Server需要手动添加volatile保证线程安全。目测Client 版本牺牲了性能保证了一致性，所以多线程操作的相当于操作同一份变量。）



### 总结

同一份代码，不同机器运行结果不一致，大概率是环境问题。需要排查jdk版本。

计算机中都是讲究平衡。要性能就要牺牲其他东西，牺牲空间，牺牲一致性等等。。Server就是牺牲了一致性换取了性能的提升。