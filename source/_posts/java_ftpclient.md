---
title: 【java】FTPClient 踩坑记录（开启日志）
categories: 后端
tags: 后端
date: 2021-05-22 11:53:24
---
### 背景

> 我最近使用了apache的ftp工具包，但是遇到了非常多的坑

> ```xml
> 
> <dependency>
>     <groupId>commons-net</groupId>
>     <artifactId>commons-net</artifactId>
>     <version>3.3</version>
> </dependency>
> ```

### 结论

先说结论，我最后才发现，**只要把这个工具类的日志打开**，**很多问题就浮出水面了**。因为FTPClient本身是没有日志的，里面连接错误，权限确实，读取失败他全部都不告诉你！！其实调试很多系统都是这样，

### 调试利器之一：打开FTPClient的日志

> 其实Apache Commons Net中的所有协议实现本身就有日志，但是他不输出，需要自己去配置

1. **输出到控制台**

   相关连接：https://stackoverflow.com/questions/53426062/enable-logging-in-apache-commons-net-for-ftp-protocol

   解决方法：

   Apache Commons Net中的所有协议实现（包括`FTPClient`，派生自[`SocketClient`](https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/SocketClient.html)）都有一个方法[`addProtocolCommandListener`](https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/SocketClient.html#addProtocolCommandListener-org.apache.commons.net.ProtocolCommandListener-)。您可以将其传递[`ProtocolCommandListener`](https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/ProtocolCommandListener.html)给实现日志记录的实现。

   有一个现成的实现[`PrintCommandListener`](https://commons.apache.org/proper/commons-net/apidocs/org/apache/commons/net/PrintCommandListener.html)，可以打印提供的协议日志`PrintStream`。

   在你获取ftpClient之后，用这样的代码：

   ```java
   ftpClient.addProtocolCommandListener(
       new PrintCommandListener(
           new PrintWriter(new OutputStreamWriter(System.out, "UTF-8")), true));
   ```

   这样就可以输出到控制台了。

2. **输出到日志文件。**

   > 其实在服务器上，我们关注的是日志文件，以上是没法输出到日志文件的。我们需要改动一下。这个找了很久，终于在so上面找到了最佳最简单的方案（https://stackoverflow.com/a/57287993/6399074）

   - 创建以下类

     ```java
     import java.io.OutputStream;
     import org.slf4j.Logger;
     public class LogOutputStream extends OutputStream {
         private final Logger logger;
         /** The internal memory for the written bytes. */
         private StringBuffer mem;
         public LogOutputStream( final Logger logger ) {
             this.logger = logger;
             mem = new StringBuffer();
         }
         @Override
         public void write( final int b ) {
             if ( (char) b == '\n' ) {
                 flush();
                 return;
             }
             mem = mem.append( (char) b );
         }
         @Override
         public void flush() {
             logger.info( mem.toString() );
             mem = new StringBuffer();
         }
     }
     ```

   - 添加ftpClient日志

     ```JAVA
     /*log创建*/
     private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class) 
     // 或者使用@Slf4j注解
     ftpClient.addProtocolCommandListener(
                         new PrintCommandListener(
                                 new PrintWriter(new OutputStreamWriter(new LogOutputStream(log), "UTF-8")), true));
     ```

   - 查看效果（开启日志后，妈妈再也不用担心我错过什么错误了！）

     ```bash
     2021-05-22 11:00:23.354  INFO 28764 --- [pool-1-thread-1] : 开始解析文件内容
     2021-05-22 11:00:23.387  INFO 28764 --- [pool-1-thread-1] : PASV
     2021-05-22 11:00:23.387  INFO 28764 --- [pool-1-thread-1] : 
     2021-05-22 11:00:23.392  INFO 28764 --- [pool-1-thread-1] : 227 Entering Passive Mode (10,146,6,244,156,64).
     2021-05-22 11:00:23.392  INFO 28764 --- [pool-1-thread-1] : 
     2021-05-22 11:00:23.399  INFO 28764 --- [pool-1-thread-1] : RETR test2.json
     2021-05-22 11:00:23.399  INFO 28764 --- [pool-1-thread-1] : 
     2021-05-22 11:00:23.403  INFO 28764 --- [pool-1-thread-1] : 550 Permission denied.
     2021-05-22 11:00:23.403  INFO 28764 --- [pool-1-thread-1] : 
     2021-05-22 11:00:23.403  INFO 28764 --- [pool-1-thread-1] : PWD
     2021-05-22 11:00:23.403  INFO 28764 --- [pool-1-thread-1] : 
     2021-05-22 11:00:23.407  INFO 28764 --- [pool-1-thread-1] : 257 "/"
     2021-05-22 11:00:23.407  INFO 28764 --- [pool-1-thread-1] : 
     ```

   ### 调试利器之二：开启服务端的日志

   > 当你操作FTP出现了问题，密码错误，指令失败，权限不够等问题，都能在服务端的日志中体现。但是默认情况下，vsftpd是关闭日志的。

   1. 打开vsftpd配置文件

      ```BASH
      vi /etc/vsftpd/vsftpd.conf
      ```

   2. 添加以下配置

      ```BASH
      # log注释
      xferlog_std_format=YES # 是否以标准xferlog的格式书写传输日志文件
      xferlog_enable=YES # 表明FTP服务器记录上传下载的情况
      xferlog_file=/var/log/xferlog # 默认为/var/log/xferlog，也可以通过xferlog_file选项对其进行设定
      dual_log_enable=YES # 前者是wu_ftpd类型的传输日志，可以利用标准日志工具对其进行分析；后者是vsftpd类型的日志
      log_ftp_protocol=YES
      syslog_enable=NO # 是否将原本输出到/var/log/vsftpd.log中的日志，输出到系统日志
      use_localtime=YES
      vsftpd_log_file=/var/log/vsftpd.log # vsftpd_log_file所指定的文件，即/var/log/vsftpd.log也将用来记录服务器的传输情况
      ```

      > 注意每个配置仅保留一个，否则会重启失败（好像是这样，我重复配置了就重启失败了）

   3. 重启vsftpd服务

      ```BASH
      service vsftpd restart
      ```

   4. 观察服务状态

      ```bash
      service vsftpd status
      ```

   5. 查看打印的日志

      ```BASH
      tail -f /var/log/vsftpd.log
      ```

      > 注意！FTP产生的日志量比较大！请在调试完成之后及时关闭日志输出

   6. 可以指定查找日志的内容

      > 如果FTP服务端被多台客户端连接，那么输出的日志可能比较乱，我们就需要过滤一下

      ```BASH
      tail -f /var/log/vsftpd.log | grep 192.168.1.123
      ```

      

   ### 调试利器之三：日志记录每次进行FTP操作之后的响应

   > 在不知道ftpClient有日志之前，我尽量都是把```ReplyString```给打印出来，尽量获取到更多的信息。

   1. 在每次操作，比如```ftpClient.changeWorkingDirectory("/data");```或者```ftpClient.retrieveFileStream(fileName);```都读取一遍ReplyString。

      如下：```log.info("ftp操作响应：{}",ftpClient.getReplyString())```。但是ftpClient进行一个操作的时候，内部其实是发送了一堆指令的组合，如果中间某个指令出了问题，那么ftpClient并不会告诉你，所以还是需要借助上面开启的日志。

### 遇到的问题总结

- **使用```ftpClient.retrieveFileStream(fileName);```无法获取输出流、或者输出流为空**

  1. **权限不够！**请观察客户端或者服务端的FTP日志是否出现```550 Permission Denied```等字样

     解决方法：提升权限或者切换更高权限的账号！

  2. **需要切换路径！**注意！retrieveFileStream读取文件的前提是你在对应的目录下面，如果你直接传入整个路径+文件名，那样好像是读不到的。

     解决方法：```ftpClient.changeWorkingDirectory("/data/");```切换到对应的路径

- **使用```FTPFile[] ftpFiles = ftpClient.listFiles();```无法获取文件列表**

  1. 425 Failed to establish connection，如果查看客户端或者服务端的FTP日志出现改错误，那么可能是**ftp客户端使用主动模式**。

     >  相关连接：https://webcache.googleusercontent.com/search?q=cache:_XttD5Dys6AJ:https://www.programmersought.com/article/4138821711/+&cd=1&hl=en&ct=clnk

     解决方案（切换成被动模式）：

     ```ftpClient.enterLocalPassiveMode();```

- **获取文件最后修改时间出现误差**

  1. 时区不一致。

     解决方案：

     ```JAVA
     // 获取到东八区的时间
     final long l = file.getTimestamp().getTimeInMillis() + file.getTimestamp().getTimeZone().getOffset(8);
     Date date = new Date(l);
     ```

- **读取文件getReplyString返回响应码：257**

  1. 权限不够！

     解决方案：查看ftpClient发送的一系列指令的响应是什么！问题就很容易解决了

- **FTP - 550 Failed to change directory**

  1. 权限不够！！反正我遇到的是权限不够

     解决方案：查看ftpClient发送的一系列指令的响应是什么！问题就很容易解决了

### 总结

使用ftpClient操作ftp为我们提供方便的同时，也屏蔽了很多对我们调试有用的信息。其实很多时候，去调试一个不熟悉的东西，首先就是要查看日志，最开始我只是见一步走一步，一步一步的调试ftpClient的源码才发现问题所在。但是只要开启了服务端和客户端的日志，问题就好解决了！
