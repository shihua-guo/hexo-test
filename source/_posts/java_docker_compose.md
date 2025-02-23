---
title: 【后端】docker-compose 搭建ELK spring-boot
categories: 后端
tags: 后端
date: 2020.02.28 18:10:21
---

### docker-compose 搭建ELK

> 本文主要是参考别人博客，并迭代记录一些原文没有遇到或者说明的问题，供自己以后有相关问题来查看，并分享给大家
>
> 个人而言，docker-compose 相对 docker 分开部署简单，可以一步到位。所以选择docker-compose搭建。
>
> 如果有遇到什么问题，请查看最后的问题记录！

#### 环境说明

​	云机-Debian10

#### docker 安装

​	请参考：https://www.runoob.com/docker/debian-docker-install.html，讲的非常好！

#### docker-compose安装

​	请参考：https://www.runoob.com/docker/docker-compose.html，这里我也再次说明一下吧。<u>主要解决服务器从github下载会很慢的问题。所以下面将直接从github下载，再复制到服务器上</u>

1. 直接从github安装

   下载地址：https://github.com/docker/compose/releases，选定一个版本然后下载。

2. 下载[docker-compose-Linux-x86_64](https://github.com/docker/compose/releases/download/1.25.4/docker-compose-Linux-x86_64) linux的版本

![1582798022(1).jpg](/img/java/13-0.png)


3. 将下载的文件复制到云机上（使用scp或者用图形化界面都可以）

    ```bash
   scp ./docker-compose-Linux-x86_64 root@192.168.1.13:/usr/local/bin/docker-compose
   ```

4. 赋予可执行权限

   > 如何debian提示sudo command not found，直接不用sudo或者安装一个：apt-get install sudo 

   ```bash
   sudo chmod +x /usr/local/bin/docker-compose
   ```

5. 创建软链

   ```BASH
   sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
   ```

6. 测试安装是否成功

   ```bash
   docker-compose --version
   ```

   

#### 安装ELK环境

- **安装ELK**

  - **创建ELK的docker-compose目录**

    ```bash
    mkdir /usr/software/elk
    ```

  - **从github拉取搭建ELK的脚本**

    > [该项目](https://github.com/deviantony/docker-elk)是使用Docker和Docker Compose 运行最新版本的[Elastic Stack]

    ```bash
    git clone https://github.com/deviantony/docker-elk.git
    ```

  - **进入刚刚clone下来的文件夹**

    ```bash
    cd docker-elk
    ```

  - **启动容器**

    > 首次启动最好不要后台启动（守护进程），我们最好直接查看他启动的日志情况：

    方式1（可以查看到日志，**首次创建最好使用这个**，等观察启动没问题再使用后台启动）：

    ```bash
    docker-compose up
    ```

    方式1（守护进程）：

    ```bash
    docker-compose up -d
    ```

    一般控制台没有异常输出，那么就代表启动成功了：

    ```bash
    Successfully started Logstash API endpoint {:port=>9600}
    ```

    查看docker容器运行状况：

    ```bash
    docker ps
    ```

    ![1582855756(1).jpg](/img/java/13-1.png)


  各个服务端口运行情况如下：

  | 名称                        | 端口 | 说明                                                         |
    | --------------------------- | ---- | ------------------------------------------------------------ |
    | Logstash TCP input          | 5000 | 用于应用传入数据进入logstash的端口                           |
    | Elasticsearch HTTP          | 9200 | 是ES节点与外部通讯使用的端口。它是http协议的RESTful接口;各种CRUD操作都是走的该端口,如查询：http://localhost:9200/user/_search |
    | Elasticsearch TCP transport | 9300 | 是ES节点之间通讯使用的端口。它是tcp通讯端口，集群间和TCPclient都走的它。java程序中使用ES时，在配置文件中要配置该端口 |
    | Kibana                      | 5601 | Kibana的Web界面入口                                          |

  - **登入Kibana 界面**

    下面就是可以见到成果的时候了！在网页输入服务器的地址：http://192.168.1.13:5601。

    登入默认的就是es的用户密码，用户名：elastic，密码：changeme

    **如果是云机的外网ip，而且没办法连通的话，那么优先考虑云机服务商的安全组的配置，再考虑防火墙，Debian本身的没有开启防火墙的。所以，麻烦到云服务商的安全组配置，开放5601接口**

- **配置ELK参数**，让spring boot应用的日志可以输入到logstash中

  - 进入logstash配置文件所在目录

    ```bash
    cd /home/xijie/app/myelk/dokcer-elk/logstash/pipeline
    ```

  - 打开配置文件

    ```bash
    vim logstash.conf
    ```

  - 修改内容如下：

    > 这里和原来的博文区别就在于，添加了es的权限验证，因为docker-compose中默认开启了 X-Pack的安全验证，如果不添加启动会没办法连接上的

    ```JSON
    input{
            tcp {
                    mode => "server"
                    port => 5000
                    codec => json_lines
                    tags => ["data-http"]
            }
    }
    filter{
        json{
            source => "message"
            remove_field => ["message"]
        }
    }
    output{
        if "data-http" in [tags]{
            elasticsearch{
                hosts=> ["elasticsearch:9200"]
                index => "data-http-%{+YYYY.MM.dd}"
                user => "elastic"
                password => "changeme"
            }
            stdout{codec => rubydebug}
        }
    }
    ```

    参数说明[参考文章](https://www.elastic.co/guide/en/logstash/current/index.html):

    | 标签                            | 说明                                                         |
    | ------------------------------- | ------------------------------------------------------------ |
    | **input**（logstash进数据接口） | 使用的是tcp，说明springboot客户端需要将日志传递到该接口，该接口正是logstash服务器接口。 |
    | **filter**(数据过滤器)          | 将message字段去掉，只是为了当展示springboot的http请求接口的数据更加规整，而不是全部展示在message字段中 |
    | **output**（数据出去接口）      | 将数据传递给了elasticsearch，这里使用了if，当判断所出数据为所指定tag，才进行下面的配置。特别要注意index的配置，该值在kibana中需要使用，这里指定的index值为：**data-http**，要注意该值与tag是没有关系的，要注意区分。 |

  - 返回源目录，重启容器

    ```bash
    cd /usr/software/elk/docker-elk
    
    docker-compose restart
    ```

- **配置spring-boot应用**

  - 添加logstash依赖

    ```xml
    <dependency>
            <groupId>net.logstash.logback</groupId>
             <artifactId>logstash-logback-encoder</artifactId>
             <version>5.2</version>
    </dependency>
    ```

  - 添加logstash配置，修改/添加**logback.xml**文件，内容如下：

    > 这里尽量简单，就是为了打通spring-boot和logstash，打通了一切都好理解了。

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
        <include resource="org/springframework/boot/logging/logback/base.xml" />
        <root level="INFO">
            <appender-ref ref="CONSOLE" />
        </root>
        <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
            <!--配置logStash 服务地址，可以写成配置的形式，但是为了方便，就直接写死在这里-->
            <destination>192.168.1.13:5000</destination>
            <!-- 日志输出编码 -->
            <encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder" />
        </appender>
        <!--定义elk日志的名称，需要上传的则使用该logger-->
        <logger name="elk_logger" level="INFO" additivity="false">
            <appender-ref ref="LOGSTASH"/><!--输出到logstash-->
            <appender-ref ref="CONSOLE"/><!--同时输出到控制台-->
        </logger>
    </configuration>
    
    ```

  - 在业务代码中，使用配置好的**elk_logger**打印，就可以同时把日志输出到logstash，并且存入ES了。

    ```java
    ....
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    ....
    private static Logger logger = LoggerFactory.getLogger("elk_logger");//必须是这个名字
    
    ...
    logger.info("我是一个简易的spring-boot连接ELK的应用");
    ...
    ```

    以上完整的例子代码在这里：[github](https://github.com/shihua-guo/elk-demo)

- **配置Kibana**，现在只要服务器通过指定的Tag打印日志，日志信息将会上传logstash解析，并且存储到elasticsearch，然后只需要kibana配置对应的elasticsearch的index即可看到所需的日志信息。

  通过浏览器访问kibana，http://192.168.1.13:5601，然后点击最下面的**management**，再点击**Kibana**的**index_patterns**

![1582877894(1).jpg](/img/java/13.png)


​		创建**index_patterns**

​		![1582878470(1).jpg](/img/java/14.png)


​		接下来，在**index pattern**框中填入上述创建ElK时logstash配置文件中的index信息：**data-http-***

![1582878795(1).jpg](/img/java/15.png)


​		**注意！如果ES没有数据是无法添加索引的！所以要确保之前的操作能够成功，如果不能写入，请查看文章最下面的参考。**

​		选择@timestamp字段作为时间筛选字段。

![1582878988(1).jpg](/img/java/16.png)


​		点击最上面的指南针的图标（Discover），然后选中data-http*索引就能够看到对应的日志了。

​		![1582879209(1).jpg](/img/java/17.png)


**以上，spring-boot结合ELK的系统就搭建完成了！**



#### 错误记录

> 我在搭建的过程中，遇到了以下的坑，浪费了我比较多时间

1. **spring-boot无法写入logstash**，但是spring-boot应用能正常启动，Kibana页面能够正常访问。

   这种情况，一般不会是应用的问题，因为应用部分配置非常简单！就算有错，在启动的时候也会非常明显，所以很有可能就是在ELK服务那边。

   **查看ELK日志**

   这一点，很重要！我就是一个个ELK服务看日志，才发现了问题，日志体现如下：不断的刷日志；出现以下错误：

   ```java
   [2020-02-28T08:46:26,997][ERROR][io.netty.util.concurrent.DefaultPromise.rejectedExecution] Failed to submit a listener notification task. Event loop shut down?
   java.util.concurrent.RejectedExecutionException: event executor terminated
   ```

   以上的日志的原因是，貌似有多个logstash实例同时启动在同一端口，导致了死循环。可以参考：https://github.com/elastic/elasticsearch/issues/27226，上面说```In case anyone (like me) lands here with the same error: check that you're not trying to run multiple instances of Logstash on the same port!```

   对于我，原因如下：

   我把原来的logstash的配置备份一份，文件结构如下：

   ```logstash.conf  logstash.conf.bak```

   于是，**我就把logstash.conf.bak删除，再启动就可以了！**

2. **logstash启动失败，logstash连接es失败**

   日志表现如下：

   ```
   [2020-02-27T08:34:46,451][WARN ][logstash.outputs.elasticsearch] Attempted to resurrect connection to dead ES instance, but got an error. {:url=>"http://elasticsearch:9200/", :error_type=>LogStash::Outputs::ElasticSearch::HttpClient::Pool::BadResponseCodeError, :error=>"Got response code '401' contacting Elasticsearch at URL 'http://elasticsearch:9200/'"}
   ```

   解决方案：修改logstash.conf文件，添加es的权限验证信息

   ```bash
   vi /home/xijie/app/myelk/dokcer-elk/logstash/pipeline/logstash.conf
   ```

   内容如下：

   ```json
   input{
           tcp {
                   mode => "server"
                   port => 5000
                   codec => json_lines
                   tags => ["data-http"]
           }
   }
   filter{
       json{
           source => "message"
           remove_field => ["message"]
       }
   }
   output{
       if "data-http" in [tags]{
           elasticsearch{
               hosts=> ["elasticsearch:9200"]
               index => "data-http-%{+YYYY.MM.dd}"
               user => "elastic"
               password => "changeme"
           }
           stdout{codec => rubydebug}
       }
   }
   ```

   以上关键在于，添加了es的user和password：

   ```bash
    elasticsearch{
        hosts=> ["elasticsearch:9200"]
        index => "data-http-%{+YYYY.MM.dd}"
        user => "elastic"
        password => "changeme"
    }
   ```

   #### 总结

   ​	通过docker-compose能够快速的搭建ELK环境，然后再通过logstash的jar可以非常方便的将应用的日志推送到logstash，下面我会继续熟悉Kibana的使用。

   #### 参考

   [docker部署ELK(logstash、elasticsearch、kibana)，监控日志](https://www.centos.bz/2019/01/docker部署elklogstash、elasticsearch、kibana，监控日志/)
   https://github.com/deviantony/docker-elk/issues/446
