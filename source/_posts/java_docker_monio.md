---
title: 【后端】Docker 安装Minio 设置免token下载
categories: 后端
tags: 后端
date: 2021.03.16 09:40:49
---
# Docker 安装Minio 设置免token下载

## 安装

> MinIO 是一个基于Apache License v2.0开源协议的对象存储服务。MinIO是一个非常轻量的服务,可以很简单的和其他应用的结合，类似 NodeJS, Redis 或者 MySQL

1. **拉取docker镜像**

   ```BASH
   docker pull minio/minio
   ```

2. **运行镜像**

   ```BASH
   docker run -p 9000:9000 \
     --name minio1 \
     -v /mnt/data:/data \
     -e "MINIO_ROOT_USER=admin" \
     -e "MINIO_ROOT_PASSWORD=123465" \
     minio/minio server /data
   ```

   命令说明：将**minio/minio**镜像别名成**minio1**运行**9000**端口并映射到物理机上，并设置minio的用户名和密码（用于上传登录）

   如果以上命令成功，可以使用下列检验：

   查看日志：``` docker logs -f minio1```

   登录web端：```http://192.168.1.12:9000/``` ，账号密码就是上面设置的那个

   上传文件：**点击右下角的加号**---->**create bucket（创建桶）**-----> **upload file（上传文件）**



## 使用

> 下面主要提供java使用例子

1. 新建一个spring boot 工程

2. 引入pom

   ```xml
           <dependency>
               <groupId>io.minio</groupId>
               <artifactId>minio</artifactId>
               <version>7.1.0</version>
           </dependency>
   ```

3. 建立上传的controller

   ```JAVA
   
       @PostMapping("/upload")
       @ResponseBody
       public String upload(@RequestParam("file") MultipartFile file) throws Exception{
           // 创建连接对象
           MinioClient minioClient = MinioClient.builder()
                   .endpoint("http://192.168.1.12:9000")
                   .credentials("admin","12345")
                   .build();
           // 前面创建桶的名称
           String bucketName = "public";
           // 原文件名
           String fileName = file.getOriginalFilename();
           // 文件类型
           String suffixName = fileName.substring(fileName.lastIndexOf("."));
           // 生成新文件名，确保唯一性
           String objectName = UUID.randomUUID().toString() + suffixName;
           // 文件类型
           // 使用putObject上传一个文件到存储桶中
           minioClient.putObject(
                   PutObjectArgs.builder()
                   .bucket(bucketName)
                   .object(objectName)
                   .stream(file.getInputStream(),-1,10485760)
                   .build());
           // 得到文件 url
           String imageUrl = minioClient.getObjectUrl(bucketName, objectName);
           // 生成下载链接
           return imageUrl;
       }
   ```

   需要填写的参数：minio的地址、验证信息、桶名称。

   最后返回的就是下载地址。

4. 使用postman或者swagger上传文件。返回资源的下载地址后，**你会发现，并没办法下载，因为这时候需要验证！**！下面我们需要设置对应的桶进行免登陆下载



## 设置桶的policy【windows环境下】

> 不同的桶需要设置不同的权限范围，这里我们需要使用一个客户端工具minioclient。【windows环境下】

1. 下载mc.exe

   ```BASH
   https://dl.min.io/client/mc/release/windows-amd64/mc.exe
   ```

2. 运行mc.exe。在下载好的文件夹，打开bash工具

   ```
   ./mc.exe
   ```

   也可以将对应文件夹添加到环境变量，就可以直接使用mc命令了。

   运行完成可以看到以下信息

   ```BASH
   Name:
     mc.exe policy - manage anonymous access to buckets and objects
   USAGE:
     mc.exe policy [FLAGS] set PERMISSION TARGET
     mc.exe policy [FLAGS] set-json FILE TARGET
     mc.exe policy [FLAGS] get TARGET
     mc.exe policy [FLAGS] get-json TARGET
     mc.exe policy [FLAGS] list TARGET
   ```

3. 添加对应的minio服务端

   ```BASH
   ./mc.exe alias set myminio/ http://192.168.1.12:9000 admin 123465
   ```

   命令说明：

   添加服务地址为：```http://192.168.1.12:9000```，**用户名和密码：admin 12345**的minio服务，**并别名成：myminio**，这里非常关键！！以后设置规则都是用**myminio**这个前缀设置！！所以你发现复制了网上的命令都设置失败的原因！！

4. 针对对应的桶设置成免token下载

   ```BASH
   ./mc.exe policy set download myminio/public
   ```

   执行成功则显示：

   ```BASH
   Access permission for `myminio/public` is set to `download`
   ```

   这样，我们就可以直接使用资源地址进行下载了！！

   资源对应的下载地址就是：地址+桶名+文件名：

   比如：```http://192.168.1.12:9000/public/6bf0aa56-b1df-407f-b740-3ab41cad87e1.jpg```



## 错误提示

1. 在使用sdk的时候，提示```valid part size must be provided when object size is unknown```

   上传的时候必须设置分块大小。对象大小直接-1，分块大小[5m,5G]，参数的单位是B，所以最小单位是：5 * 1024 * 1024 = 5242880。minio支持分块传输，按照下面创建即可

   ```JAVA
   PutObjectArgs.builder()
   .bucket(bucketName)
   .object(objectName)
   .stream(file.getInputStream(),-1,5242880)
   .build()
   ```

   下面的minio说明

   ```JAVA
   	/**
        * Sets stream to upload. Two ways to provide object/part sizes.
        *
        * <ul>
        *   <li>If object size is unknown, pass -1 to objectSize and pass valid partSize.
        *   <li>If object size is known, pass -1 to partSize for auto detect; else pass valid partSize
        *       to control memory usage and no. of parts in upload.
        *   <li>If partSize is greater than objectSize, objectSize is used as partSize.
        * </ul>
        *
        * <p>A valid part size is between 5MiB to 5GiB (both limits inclusive).
        */
   ```

2. 设置policy的时候提示：``` <ERROR> Incorrect number of arguments for alias set command. Invalid arguments provided, please refer `mc <command> -h` for relevant documentation.```

   这个应该就是桶写错了，或者minio的服务别名写错！！

## 附录

不同的规则说明：

```
  1. Set bucket to "download" on Amazon S3 cloud storage.
     C:\> mc.exe policy set download s3/burningman2011

  2. Set bucket to "public" on Amazon S3 cloud storage.
     C:\> mc.exe policy set public s3/shared

  3. Set bucket to "upload" on Amazon S3 cloud storage.
     C:\> mc.exe policy set upload s3/incoming

  4. Set policy to "public" for bucket with prefix on Amazon S3 cloud storage.
     C:\> mc.exe policy set public s3/public-commons/images

  5. Set a custom prefix based bucket policy on Amazon S3 cloud storage using a JSON file.
     C:\> mc.exe policy set-json /path/to/policy.json s3/public-commons/images

  6. Get bucket permissions.
     C:\> mc.exe policy get s3/shared

  7. Get bucket permissions in JSON format.
     C:\> mc.exe policy get-json s3/shared

  8. List policies set to a specified bucket.
     C:\> mc.exe policy list s3/shared

  9. List public object URLs recursively.
     C:\> mc.exe policy --recursive links s3/shared/

```



