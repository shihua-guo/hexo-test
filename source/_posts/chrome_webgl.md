---
title: 【前端】chrome下旧显卡开启WebGL
categories: 前端
tags: 前端
date: 2021.03.25 13:51:53

---
# chrome下旧显卡开启WebGL

## 背景

> 我使用的电脑的显卡是R5 230显卡，是一款比较旧的亮机卡。chrome版本为：版本 89.0.4389.90（正式版本） （64 位）

打开某些网页的时候提示：The browser supports WebGL, but initialization failed. 但是在WebGL测试网站显示是支持的。

如：https://webglreport.com/?v=2、https://get.webgl.org/。


![image-20210325134212678.png](/img/front/37.webp)


## 原因

被浏览器屏蔽了。屏蔽说明如下：

https://www.khronos.org/webgl/wiki/BlacklistsAndWhitelists 

```js
Firefox on Windows
For WebGL in Firefox on Windows, Windows XP or newer is required. The following minimal driver versions are required: either NVIDIA >= 257.21, or ATI/AMD >= 10.6, or Intel driver versions from September 2010 or newer.

Chrome on Windows
On all versions of Windows, WebGL is disabled on all graphics drivers older than January 1, 2009.
Additionally, on Windows XP, WebGL is disabled on ATI/AMD drivers older than version 10.6, on NVIDIA drivers older than version 257.21, and on Intel drivers older than version 14.42.7.5294.
WebGL is disabled on NVIDIA driver 6.14.11.9621 on Windows XP.
WebGL is always disabled on ATI FireNV 2400.
WebGL is disabled on Parallels drivers older than 7.
WebGL is always disabled on S3 Trio cards.
```



## 方案

1. 更换成火狐浏览器，火狐浏览器要求更低。或者IE浏览器。

2. 如果是谷歌浏览器，可以吧```webglblocklist```关闭了。

   1. 打开```chrome://flags/```

   2. 找到```[ignore-gpu-blocklist](chrome://flags/#ignore-gpu-blocklist)```，如下：

![image-20210325134739129.png](/img/front/38.webp)
  设置成：enabled

   3. 开启硬件加速，如下：


![image-20210325134824753.png](/img/front/39.webp)


   4. 重启chrome

      

## 总结

显卡本身是支持webgl的，但是就是被浏览器屏蔽了，开启即可。