---
title: 【后端】搭建canal并对接RabbitMQ
categories: 后端
tags: 后端
date: 2021-10-20 11:27
---
# 搭建canal并对接RabbitMQ

> canal是非常好用的mysql数据增量同步工具
>
> 安装其实按照官方文档就可以了，看其他博客，难免会有差错，只会徒增不必要的时间
>
> 官方安装文档地址：[QuickStart · alibaba/canal Wiki (github.com)](https://github.com/alibaba/canal/wiki/QuickStart)



### 目标

1. 准备工作（配置mysql、新建canal用户）
2. 安装canal
3. 配置canal
4. 运行canal
5. 观察canal成功接入RabbitMQ



## 按部就班执行

#### 准备工作

- 对于自建 MySQL , 需要先开启 Binlog 写入功能，配置 binlog-format 为 ROW 模式，my.cnf 中配置如下

  ```bash
  [mysqld]
  log-bin=mysql-bin # 开启 binlog
  binlog-format=ROW # 选择 ROW 模式
  server_id=1 # 配置 MySQL replaction 需要定义，不要和 canal 的 slaveId 重复
  ```

  - 注意：针对阿里云 RDS for MySQL , 默认打开了 binlog , 并且账号默认具有 binlog dump 权限 , 不需要任何权限或者 binlog 设置,可以直接跳过这一步

- 授权 canal 链接 MySQL 账号具有作为 MySQL slave 的权限, 如果已有账户可直接 grant

  ```mysql
  CREATE USER canal IDENTIFIED BY 'canal';  
  GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
  -- GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' ;
  FLUSH PRIVILEGES;
  ```



#### 安装canal

- 下载 canal, 访问 [release 页面](https://github.com/alibaba/canal/releases) , 选择需要的包下载, 如以 1.0.17 版本为例

  ```bash
  wget https://github.com/alibaba/canal/releases/download/canal-1.0.17/canal.deployer-1.0.17.tar.gz
  ```

- 解压缩

  ```bash
  mkdir /tmp/canal
  tar zxvf canal.deployer-$version.tar.gz  -C /tmp/canal
  ```

  - 解压完成后，进入 /tmp/canal 目录，可以看到如下结构

    ```
    drwxr-xr-x 2 jianghang jianghang  136 2013-02-05 21:51 bin
    drwxr-xr-x 4 jianghang jianghang  160 2013-02-05 21:51 conf
    drwxr-xr-x 2 jianghang jianghang 1.3K 2013-02-05 21:51 lib
    drwxr-xr-x 2 jianghang jianghang   48 2013-02-05 21:29 logs
    ```



#### 配置canal

- 配置`/tmp/canal/canal.properties`

  > 需要提前获取mq的连接信息，并建立对应的exchange，否则启动会报错

  ```bash
  # tcp, kafka, rocketMQ, rabbitMQ
  canal.serverMode = rabbitMQ # 这里改成rabbitMQ模式
  
  
  ##################################################
  #########                   RabbitMQ         #############
  ##################################################
  # 配置rabbitMQ连接信息
  rabbitmq.host = 192.168.1.1
  rabbitmq.virtual.host = /
  rabbitmq.exchange = exchange.fanout.canal # exchange的名字，需要提前新建好
  rabbitmq.username = admin
  rabbitmq.password = admin
  rabbitmq.deliveryMode = fanout # exchange的模式
  
  ```

- 配置`/tmp/canal/instance.properties`

  可以登录mysql，执行`show master status;`，字段如下：

  | File             | `Position` | Binlog_Do_DB | Binlog_Ignore_DB                                       | Executed_Gtid_Set |
  | ---------------- | ---------- | ------------ | ------------------------------------------------------ | ----------------- |
  | mysql-bin.004911 | 471834950  |              | mysql,mysql,information_schema,performation_schema,sys |                   |

  `canal.instance.master.journal.name`就是File字段【mysql-bin.004911】binlog文件名

  `canal.instance.master.position`就是binlog的偏移的位置【471834950】

  ```BASH
  ...
  # 填写mysql信息
  # position info
  canal.instance.master.address=192.168.1.1:3306
  canal.instance.master.journal.name=mysql-bin.004911
  canal.instance.master.position=471834950
  canal.instance.master.timestamp=
  canal.instance.master.gtid=
  
  ...
  # 填写mysql用户信息
  # username/password
  canal.instance.dbUsername=canal
  canal.instance.dbPassword=canal
  
  # 指定监听某个库、表
  canal.instance.filter.regex=.*\\..* # 【.*\\..*】则是监听所有的库表。【test\..*】代表监听test库下面的所有表。
  ```

  

#### 启动canal

- 运行`sh /tmp/canal/bin/startup.sh`

- 查看 server 日志

  ```bash
  tail -f /tmp/canal/logs/canal/canal.log</pre>
  ```

  ```log
  2013-02-05 22:45:27.967 [main] INFO  com.alibaba.otter.canal.deployer.CanalLauncher - ## start the canal server.
  2013-02-05 22:45:28.113 [main] INFO  com.alibaba.otter.canal.deployer.CanalController - ## start the canal server[10.1.29.120:11111]
  2013-02-05 22:45:28.210 [main] INFO  com.alibaba.otter.canal.deployer.CanalLauncher - ## the canal server is running now ......
  ```

- 查看 instance 的日志

  ```bash
  tail -f /tmp/canal/logs/example/example.log
  ```

  ```log
  2013-02-05 22:50:45.636 [main] INFO  c.a.o.c.i.spring.support.PropertyPlaceholderConfigurer - Loading properties file from class path resource [canal.properties]
  2013-02-05 22:50:45.641 [main] INFO  c.a.o.c.i.spring.support.PropertyPlaceholderConfigurer - Loading properties file from class path resource [example/instance.properties]
  2013-02-05 22:50:45.803 [main] INFO  c.a.otter.canal.instance.spring.CanalInstanceWithSpring - start CannalInstance for 1-example 
  2013-02-05 22:50:45.810 [main] INFO  c.a.otter.canal.instance.spring.CanalInstanceWithSpring - start successful....
  ```

- 关闭

  ```
  sh /tmp/canal/bin/stop.sh
  ```

  如果关闭提示：`存在canal.pid`，但是进程又没有。可以直接删除`canal.pid`，在启动就可以了



#### 观察RabbitMQ

1. 观察日志`tail -f /tmp/canal/logs/example/example.log`

2. 观察MQ情况：新建一个queue对接exchange，然后看看能否接受到数据

   

#### Q&A

1. 提示`found canal.pid , Please run stop.sh first ,then startup.sh`

   A：该文件是记录了canal运行的pid号，用户stop的时候kill对应的pid。应该是没有执行`stop.sh`，需要执行一下`stop.sh`。如果执行了`stop.sh`，该文件还没删除，但是对应的进程已经没有了，可以直接删除该文件。

2. 提示

   ```bash
   Unrecognized VM option 'UseCMSCompactAtFullCollection'
   Error: Could not create the Java Virtual Machine.
   Error: A fatal exception has occurred. Program will exit.
   ```

   A：可能JDK版本太高【UseCMSCompactAtFullCollection已经被移除了】，建议使用jdk8。

3. 提示：

   ```bash
   2021-10-19 16:31:26.803 [pool-6-thread-1] ERROR c.a.o.c.c.rabbitmq.producer.CanalRabbitMQProducer - com.rabbitmq.client.AlreadyClosedException: channel is already closed due to channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'exchange.trade.order' in vhost '/', class-id=60, method-id=40)
   java.lang.RuntimeException: com.rabbitmq.client.AlreadyClosedException: channel is already closed due to channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'exchange.trade.order' in vhost '/', class-id=60, method-id=40)
   ```

   A：没有建立对应的exchange，请到mq管理中心创建。



#### 总结

	canal搭建起来还是十分简单的，只要理解他的基本功能：伪装成mysql的一个从节点，请求master dump binlog数据给他，然后再把数据解析推送到mq中。所以我们只需要配置mysql和mq两边的配置即可。但是我遇到的坑都是搭建mq还有jdk的问题，还有服务器依赖，证书等乱七八糟的问题。canal的问题基本没有，一下子就搭起来了。