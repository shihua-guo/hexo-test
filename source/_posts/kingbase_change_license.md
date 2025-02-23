---
title: 【数据库】人大金仓 kingbase 更换license
categories: 数据库
tags: 数据库
date: 2021.04.14 09:39
---
# 人大金仓 过期 更换license

> 在网上找不到对应资料，如何简单快速的更换人大金仓license

## 背景

启动数据库失败，于是查看日志：/data/kingbase/ES/V8/data/sys_log/startup.log，提示如下：

```BASH
FATAL:  XX000: License file expired.
LOCATION:  PostmasterMain, postmaster.c:623
FATAL:  XX000: License file expired.
LOCATION:  PostmasterMain, postmaster.c:623
FATAL:  XX000: License file expired.
LOCATION:  PostmasterMain, postmaster.c:623
FATAL:  XX000: License file expired.
LOCATION:  PostmasterMain, postmaster.c:623

```

## 解决方案

1. 去人大金仓官网，点击下载

2. 回复：```软件下载```

3. 得到以下链接

   ```
   一、KingbaseES V8 (R3)
   https://pan.baidu.com/s/1O3mCV8nZUF6Yw4eH2MSEkw 
   提取码：okif 
   
   KingbaseES V8 (R2)
   https://pan.baidu.com/s/1wmFy8-fMMbzgJ9E15RBzaA
   提取码：jw2b
   
   二、KADB
   链接：https://pan.baidu.com/s/18T--ETxZim-9ip5nkF2vaA 
   提取码：j274
   ```

4. 下载对应版本的license.dat

   进入：https://pan.baidu.com/s/1O3mCV8nZUF6Yw4eH2MSEkw 

   下载，license-V8R3-企业版-90天.zip

   解压后可以看到license文件：

   ```license_4392_0.dat```

5. 上传到服务器，进行替换原有的license.dat

   ```bash
   cd /data/kingbase/ES/V8/				#进入license目录	
   mv license.dat  license.dat.bak			#备份原有license.data
   mv license_4392_0.dat license.dat		#重命名新的license
   chown kingbase:kingbase license.dat		#更改license文件拥有者
   sudo systemctl start kingbase			#启动金仓数据库
   ```

## 总结

网上资料太少，这个我也是摸索出来的比较快的方案。