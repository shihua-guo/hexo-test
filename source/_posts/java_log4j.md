---
title: 【java】Log4j转OutputStream
categories: 后端
tags: 后端
date: 2021-05-22 12:09:00
---
> 有时候，一些框架提供了日志输出，但是需要我们制定输出流，才会帮我们打印，下面讲解一下如何获取log4j的输出流
>
> spring boot下

1. 新建以下类

   ```JAVA
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

2. 获取输出流

   ```java
   private org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class) 
   // log可以新建也可以使用注解
   new LogOutputStream(log);
   ```

