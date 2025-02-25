---
title: 【前端】谷歌浏览器调试工具的13个Tips
categories: 前端
tags: [前端,浏览器]
date: 2019.01.20 19:04:22
pin: true
---
## 下面介绍如何更加高效的调试前端
> chrome版本：版本 71.0.3578.98（正式版本） （64 位）

####  Tips1：**console**面板可以直接运行js
 有时候我们想测试某段代码，就可以直接在console面板中执行就可以了。不需要再建立js，然后刷新浏览器。

![1.png](/img/front/9.webp)
如图，我直接输入console.log(123)，可以直接看到结果。console就是一个小小的Snippet运行器。比如我想看A的Ascii码，直接F12，然后再console运行**"A".charCodeAt(0);**，你就可以获得你需要的。有时候，一些简单的数据处理（排序，归集）之类的，我就是直接在console处理的，非常快就能获得你需要的结果。~~或者你去**Network**面板，截取网站的js，然后修改js，直接在**console**面板运行，就可以达到修改源js的效果。~~



####  Tips2：快速到达某个元素的标签处。
以前我是这样选中一个页面元素的：
~~F12打开调试面板、开启元素选择器、选中需要的元素~~。
然后，我发现可以直接对该元素右击，检查就可以直接到达对应元素标签处了。
![选择元素2.gif](/img/front/10.webp)


####  Tips3：快速清除指定网站的Cookie
因为chrome保存了很多我需要的cookie，所以我需要针对特定的网站进行清除。以前我是这样做的：
**打开设置、高级、内容设置、Cookie、查看所有 Cookie 和网站数据
、搜索需要清除网站的cookie、点击删除**。
现在我是这样做的：
直接点击地址栏前面的**“查看网站信息”**、点击Cookie、清除。
![删除cookie.gif](/img/front/11.webp)
不过这样子就只能删除当前网站相关的cookie。
### 插件篇
####  Tips4：快速清除缓存
以前我清除缓存是：Ctrl+Shift+Delete（打开设置、高级、清除浏览数据）、勾选需要清除的数据、点击清除。
现在，我需要一个插件[Clear Cache Shortcut](https://chrome.google.com/webstore/detail/clear-cache-shortcut/jnajhcakejgchhbjlchkfmdidgjefleg?hl=en)。安装完成之后，只需要点击一下，就可以清除你之前**勾选的需要清除的数据**（就像之前我只勾选了**缓存的图片和文件**，那么点击就只帮你清除该项）。
其实还能更快，我们为这个插件添加一个**快捷键**：
地址栏打开[chrome://extensions/shortcuts](chrome://extensions/shortcuts)
![清除缓存.gif](/img/front/12.webp)
之后，每次需要清除缓存，只需要按快捷键就可以清除了。

#### Tips5：快速切换至指定分辨率
这里需要一个插件：**[Window Resizer](https://chrome.google.com/webstore/detail/window-resizer/kkelicaakdanhinjdeammmilcgefonfh?hl=en)**
![分辨率.gif](/img/front/13.webp)
同样，我们可以为各个分辨率设置对应的快捷键。所以开发的时候就可以快速切换了。
地址栏打开[chrome://extensions/shortcuts](chrome://extensions/shortcuts)，找到**Window Resizer**对应的快捷键。然后设置，设置完成之后，就可以使用了。预设好，开发的时候就能快速查看在各个分辨率下的效果了。
![file.gif](/img/front/14.webp)

#### Tips6：重新发起XHR请求
>有时候我们需要调试后台的时候，需要让前台发起请求。大多数我们都是点击按钮触发（比如说保存，再点一次保存按钮就可以了）。不过如果那个请求没有绑定事件呢，如果想再次触发需要刷新页面，流程都跑 一遍。

不过，我们可以让某个请求重新发起。只需要进入**Network面板、右键需要发起的请求、Replay XHR**
![xhr.gif](/img/front/15.webp)


### 调试篇
#### Tips7：通过console面板查看全局变量相关的代码
有时候，我们想查看某个全局变量或者函数的代码，那么就可以通过**console面板**输入想查看的函数名称，然后双击，就可以直接到达对应的代码块了。比如下面，我想查看**QRCode**函数是怎么实现的，我就在**console**输入**QRCode**，然后双击，就可以了。同时，如果查看到压缩的代码，可以点击左下角的 **{}** ，chrome就帮你格式化了。
![qrcode.gif](/img/front/16.webp)

#### Tips8：通过请求查看发出请求的函数调用栈
>有时候，我们调试别人写的代码，但是无从下手。我调试一般是从请求下手，然后通过请求能够找到对应代码处。

将鼠标移至需要查看请求的initiator处，就可以看到调用这个请求的全过程。然后如果是ajax请求，一般就在**ajax的上面一层**。或者通过文件名就可以大概看到哪一个是执行该请求的文件。然后点击进去，就能直接定位到发出请求的地方。然后就可以分析、调试代码了。
![请求源码.gif](/img/front/17.webp)

#### Tips9：快速查看相关的代码[传统引入，非webpack构建]
>很多时候，代码是我们自己写的，但是我们并不知道相关的JavaScript在哪里？所以只要我们知道该js文件的名称，就能快速达到想要查看的代码。

比如，下面的很简单的结构。我们引人了**test.js**的文件。那么，调试该怎么下手呢？其实在chrome的page栏，为我们保存了原始的文件结构。我们只需要打开需要调试的资源文件。
![文件结构.jpg](/img/front/18.webp)

打开调试面板、打开source栏、点击**open file**（或者**ctrl+p**）、输入**test.js**，然后就可以开始进行调试了。
![打开资源文件.gif](/img/front/19.webp)

#### Tips10：开发时，通过Resource查看相关的代码[webpack构建]
>个人接触的webpack项目不多。不知道是否会受webpack的配置影响。不过我这里配置下是可以这样调试的。

在webpack打包的项目中，因为代码都被**压缩混淆**了，所有我们调试有时候也是无从下手。不过，chrome也为我们完整的保留了压缩混淆前的**代码结构**。在**Page**下面的 **webpack://.** 栏目下。我们可以完整的看到原始项目的结构以及组件了。我们就可以针对需要的组件进行调试了。**不过，值得注意的是**，当我们修改这些**组件**的代码之后再运行，会发现还是运行原来的。因为chrome正在运行还是压缩混淆之后的代码。而你修改的只是副本之类的。
**当然了，我们有更好的选择**，那就是前端框架**自带的调试工具**。我几乎都是使用调试工具进行调试了。
![webpack_Trim.gif](/img/front/20.webp)



#### Tips11：不需要使用console来watch变量
>以前，我调试都是通过console，将变量打印出来，当然这个是一个调试的好办法。不过呢，chrome一直就有一个调试面板，我们可以尝试使用它。

通过以上或者更多的方法，我们就可以到达了我们需要调试的地方。然后设置需要调试的断点。就可以像普通的IDE一样进行调试。下面，我这是了一个全局变量和局部变量。在断点处都能看到这些变量的情况。然后在右侧的watch中也能看到当前的局部变量和全局变量。**另外，我们也可以直接修改代码，然后ctrl+s保存，然后运行的就是我们修改之后的代码了。**
![var.gif](/img/front/21.webp)


----
#### Tips12：调试利用**Call Stack**，查看某个函数调用栈。
![ezgif-1-38af0579d0bd.gif](/img/front/22.webp)
#### Tips13：在调试中，终止当前运行的JavaScript。
长按 **Resume**按钮，就会出现一个下拉，有**Resume**和**Stop**，当你选择了**Stop**，之后整个脚本就会停止运行。
![ezgif-1-0e9220e80e5e.gif](/img/front/23.webp)

----
#### Tips13：模拟弱网环境。
打开Network面板的online选项，可以看到几种网速预设：离线，fast 3G，slow 3G
![image.png](/img/front/24.webp)
选中slow 3G之后，速度就变得非常慢，1kb的json上传了10秒
![image.png](/img/front/25.webp)

同时还支持自定义限速（点击custome --> add）：
![image.png](/img/front/26.webp)

----
#### Tips14：开启新页签自动打开console，方便调试。
有时候，我们调试某些网页是打开了新页签，如果没有及时打开console，那么我们就会错过刚刚打开新页签的那些请求，如果我们想观察那些请求怎么办呢？
- 打开console的设置
![image.png](/img/front/27.webp)
- 开启弹出页签自动打开调试工具
![image.png](/img/front/28.webp)
- 在我们需要调试的跳转前的页签，打开调试工具，然后点击跳转的时候就会自动打开调试工具了。
以上就是自己总结的经验，希望能够帮助到你更高效地开发 
~~之后想到其他的会继续更新~~  


