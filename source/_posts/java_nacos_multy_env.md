---
title: 【后端】从Nacos配置中心获取配置并实现多环境
categories: 后端
tags: 后端
date: 2021-03-17 18:28
---
# 从Nacos配置中心获取配置并实现多环境

> 微服务的配置很多是公用的，所以可以抽取公用的配置到nacos中。主要分几步：在配置中心添加相关的配置文件、添加依赖、添加配置
>
> Nacos配置支持运行时自动刷新应用中的配置，并且是全量的



## 配置过程

1. **在配置中心添加配置**

   登录nacos中心。在public命名空间下添加**common.yml**。这里注意！！dataId必须是加上配置后缀的

   比如：

   ```yml
   person:
     name: alan
   ```

2. **添加maven依赖**

   > 这里，我找了几个网上的都是写错的，搞的我下不来依赖，一直不知道是什么回事，所以要注意了

   说明：

   1. 添加了maven依赖，在不配置参数下，则默认加载namespace为**public，group为DEFAULT_GROUP、**[applicationName].yml**【具体根据配置的拓展名加载】、[applicationName]-[env].yml**【具体根据配置的拓展名加载】。

      比如：应用名为：serviceA，环境：dev，则在引导启动时加载：**serviceA.yml，serviceA-dev.yml**。

   2. nacos-config的**配置必须写在bootstrap.yml中**，因为bootstrap.yml是引导启动，在加载application.yml前加载了。

   ```xml
   <dependency>
       <groupId>com.alibaba.cloud</groupId>
       <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
       <version>2.1.0.RELEASE</version>
   </dependency>
   ```

3. 在应用的bootstrap.yml中添加配置

   ```yml
   spring:
     profiles: dev
     cloud:
       nacos:
         config:
           server-addr: 192.168.1.1:8848
           file-extension: yaml
           shared-dataids: common.yml #这里支持多个配置使用,分割。比如：a.yml,b.yml
           refreshable-dataids: common.yml #自动刷新的配置。这里支持多个配置使用,分割。比如：a.yml,b.yml
   ```

4. 验证。

   启动应用，就会发现启动日志把nacos加载的配置名称：**serviceA.yml，serviceA-dev.yml**、**common.yml**都打印了日志，并且把配置内容都打印出来。

## 多环境方案

> 网上其他博主都说了很多并且很全面的方案，但是感觉不适合我。我主要想要实现下面的东西。
>
> 1. 在nacos有一个所有微服务复用的配置
> 2. 可以在一个文件里面指定多环境

网上说多环境可以使用namespace、groupId实现，或者使用dataId后缀也是可以的。下面说下我的方案：

1. 在bootstrap.yml添加多环境配置，多环境又bootstrap决定，因为不同环境的nacos是不一样的，所以在nacos指定多环境就没有意义了

   比如：

   ```YML
   ---
   spring:
     profiles: dev
     cloud:
       nacos:
         config:
           server-addr: 192.168.8.127:8848
           file-extension: yaml
           shared-dataids: common.yml
           refreshable-dataids: common.yml
   ---
   spring:
     profiles: test
     cloud:
       nacos:
         config:
           server-addr: 192.168.8.127:8848
           file-extension: yaml
           shared-dataids: common.yml
           refreshable-dataids: common.yml
   ---
   spring:
     profiles: prod
     cloud:
       nacos:
         config:
           server-addr: 192.168.8.127:8848
           file-extension: yaml
           shared-dataids: common.yml
           refreshable-dataids: common.yml
   
   ```

   

# 一些记录

- nacos自动刷新配置是全量刷新的。但是一些链接信息是没办法刷新的（redis、mysql）。

- nacos不支持单文件多环境的配置，他会合并这些配置，并且读取最后一个。比如下面文件结构

  ```yml
  ---
  spring:
    profiles: dev
  person:
    name: dev
  ---
  spring:
    profiles: test
  person:
    name: test
  ---
  spring:
    profiles: prod
  person:
    name: prod
  ```

  拆分了多环境，但是在应用加载进来的时候，貌似会合并，我也验证了，确实会，并且会使用**最后一个配置，也就是使用prod环境的配置**。

- 如果spring-cloud-starter-alibaba-nacos-config依赖包一直没办法下载下来，请确认一下pom依赖是否写对了。再不行就到maven仓库找，比如阿里云的仓库：https://maven.aliyun.com/mvn/search，在这里你可以搜索包确认是否存在，顺便校验包名又没写错