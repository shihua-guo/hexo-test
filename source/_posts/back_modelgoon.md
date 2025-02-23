---
title: 【后端】ModelGoon：逆向UML Eclipse插件
categories: 后端
tags: 后端
date: 2016.11.13 17:38:40
---
>公司丢了一个项目让我们实习生看，然后我思路呢，可以先看看UML图，但是我们的需求说明文档上面没有，突然想起之前老师说的可以用Start UML逆向UML。然后我就试了，结果发现不支持jdk1.4以上的。然后想Eclipse上应该有插件可以逆向吧，然后就谷歌，尝试了许多许多插件和应用： *ArgoUML*，*Plant UML*，*Green UML*,*Start UML*....怎么说呢？相关的工具都比较老了，各种不好用，反人类。逆向一个普普通通的类图都需要各种繁琐步骤。 

后来发现一款很好用的，很简单的的Eclipse插件： **ModelGoon UML** 。官网：[http://www.modelgoon.org/](http://www.modelgoon.org/)
##### 下面说下安装和使用：
###### 安装
1. 直接通过Eclipse MarketPlace下载：搜索 **ModelGoon**就可以了。但是如果你访问上面的网站，你会发现登陆不上去，貌似需要科学上网。

2. 下载.jar文件，解压到Eclipse的plus-in文件夹下。重启Eclipse。

3. 通过下载压缩包来安装。
-------------------------------------------------
下面我就说说第三种。

1. **下载：ModelGoon-4.4.1-site.zip文件。**

   如果你有代理，直接访问下载：[http://www.modelgoon.org/downloads/ModelGoon-4.4.1-site.zip ](http://www.modelgoon.org/downloads/ModelGoon-4.4.1-site.zip%20) 记得在下载工具上配置代理。

   没有就用百度云吧，我刚刚上传的：链接: http://d9e.cn/JZQDt 	https://g.yam.com/MrLRI  https://s.yam.com/E94vN  https://tinyurl.com/2zwjo5jh  https://dwz.ee/3bu  提取码: t7tj    失效的话通知我一下。

2. **在Eclipse上从本地安装插件：**

  ![image](/img/java/28.png) 

3. **添加本地文件。**

      ![image](/img/java/29.png)  

4. **点击：archive**

![image](/img/java/30.png) 

5. **选择刚刚下载的“ModelGoon-4.4.1-site.zip”**

![image](/img/java/31.png) 

6. **点击OK**

![image](/img/java/32.png) 

7. **勾选，然后next（我的已经安装过了，所以点不了）**
![image](/img/java/33.png) 

8. **接受。**
![image](/img/java/34.png) 

 9. **等待。可以看右下角的进度条。**
![image](/img/java/35.png) 

 10. **安装过程，他会提示你是否确定安装不信任的插件。你点击确定就好了。**

 11. **安装完成会提示你是否重启Eclipse。那么重启就好了。**

附带官方安装方法（需要科学上网）：http://www.modelgoon.org/?page_id=75

 --------------------------------------------------------------------------------------------------------------------------------------------------------

#####使用方法：

1. 新建一个ModelGoon UML的图。ModelGoon UML提供3种UML（class diagram,interaction diagram,package dependencies diagram）。右键需要生成UML图的项目---->new---->other (或者快捷键ctrl+n)
        ![image](/img/java/36.png) 

2. 搜索：“Diagram” ，然后发现多了几个文件类型：
![image](/img/java/37.png) 

3. 选择第一个：Class Diagram，然后随便取个名。然后生成一个空白的mgc文件
![image](/img/java/38.png) 

4. 直接选中你需要生成类图的类，然后往刚刚那个文件里面拖，然后发现，它会自动帮你关联关系。
  ![image](/img/java/39.png) 

5. 目前我没发现如何让类图自动排版的方法。。。。所以需要自己一个个排好。

6. 如果需要生成时序图。那么选中其中一个类图的方法-------》右键--------》show sequence
  ![image](/img/java/40.png) 

7. 可以看到，对应的时序图了。
  ![image](/img/java/41.png) 

 附带官网使用说明：[http://www.modelgoon.org/?page_id=174](http://www.modelgoon.org/?page_id=174)
