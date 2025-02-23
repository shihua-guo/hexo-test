---
title: 【前端】arcgis导出
date: 2018-06-27 19:32:07
categories: 前端
tags: arcgis
---
# arcgis导出

> 基于arcgis javascript api 3.24

### 背景
最近需要做一个地图导出的需求。有大概一下2个要求：
1. 需要导出全市范围（也就是包括视野范围之外的也需要导出）
2. 所见即所得，当前页面看到的和导出的一致。

于是，我大致思路如下：
1. 使用arcgis自带的PrintTask工具
2. 直接将整个地图“截图”下来，保存成图片再给客户。

### 使用PrintTask
使用PrintTask比较简单，官网也有例子。主要步骤如下：
1. var printTask = new PrintTask(url);
  > 这里的url放的是这个工具的服务地址。[arcgis官网的工具地址](http://sampleserver6.arcgisonline.com/arcgis/rest/services/Utilities/PrintingTools/GPServer/Export%20Web%20Map%20Task)。
  如果本地有arcserver，那么本地的地址可以到arcgisserver 后台管理查看,进入后台管理后，点击左边的 **Utilities** 然后看到 **PrintingTools** 就是了。

  ![后台管理查看](/img/arcgis-export/1.png)

1. var params = new PrintParameters();建立导出的模板PrintParameters，具体参数去api查看。并且设置map： params.map = map;

2. 执行：printTask.execute(params, printResult=>{});这里会回调一个printResult，里面带有图片或者pdf等文件的下载地址。
  > 注意：导出可能会遇到超时问题。

但是呢，我遇到了以下问题：
1. 最严重的是，导出速度非常非常非常慢。经测试，最简单的导出需要3分钟左右。所以会遇到超时问题，这里需要设置一下esriConfig。代码如下：
  ```javascript
  import esriConfig = require("esri/config")
  ...
  esriConfig.defaults.io.timeout = 300000;//这里设置你的超时时间
  ```
  > 开始我以为是来源于PrintingTools服务的 **客户端可使用服务的最长时间** 设置造成的，其实并不是。是因为esri默认的请求时间是1分钟。

2. 导出的分辨率低，而且不能识别某些图层。比如我使用featureSet构建的图层。

以上，PrintingTools导出的速度基本就判死刑了。经我们公司的arcgis人员使用arcserver导出也是非常慢。原因不得而知，cpu/内存占用几乎没变化，并且导出会提示内存不足，但是内存十分充足。
>所以在这里请教一下大家，服务器并没有显卡,是因为gpu的问题吗，才导出这么慢吗？所以，我今天选择了第二种方法：截图。


### 截图保存
  > 有了这个想法，我就查了如何实现，并且查到相关的：[arcgis api for js入门开发系列二十打印地图的那些事](https://www.jianshu.com/p/e7b82caa12b5)。非常感谢！

主要步骤如下：
1. 获取到地图的容器->将图层元素转化为canvas->下载。
  > 这里需要用到[html2canvas](https://github.com/niklasvh/html2canvas)。
  
  实现很简单，代码如下，(完全从上面的简书搬过来的),我重点是解决上面简书并没有提及的一些问题：

  ```javascript
    canvasPrint=function () {
      var targetDom = $("#map");
      //克隆截图区域
      var copyDom = targetDom.clone();
      copyDom.width(targetDom.width() + "px");
      copyDom.height(targetDom.height() + "px");
      copyDom.attr("id", "copyDom");
      $("body").append(copyDom);
      //移除不需要截图的区域
      $(".base-map").remove();
  
      var pathName = document.location.pathname;
      var ctxPath = pathName.substring(1, pathName.substr(1).indexOf('/') + 1);
      html2canvas(copyDom[0], {
          useCORS: true,
          imageTimeout:0
          //, proxy: "/" + ctxPath + "/proxy/proxyScreenShot"
      }).then(function (canvas) {
          var url = canvas.toDataURL();
          //创建下载a标签
          var a = document.createElement("a");
          a.setAttribute("id", "download");
          document.body.appendChild(a);
          //以下代码为下载此图片功能
          var triggerDownload = $("#download").attr("href", url).attr("download", "img.png");
          triggerDownload[0].click();
          //移除下载a标签
          document.body.removeChild(a);
          //克隆DOM删除
          copyDom.remove();
      });
    }
  ```

2. 会出现以下问题
  1. 问题1：**图层会和底图出现偏移**，如图：
    ![偏移](/img/arcgis-export/2.png)

    为什么会出现偏移呢？ **因为html2canvas转化成canvas的时候并[不支持transform](https://github.com/niklasvh/html2canvas/issues/220)**
    我们看看各个图层在dom是如何构成： 
    ![构成](/img/arcgis-export/3.png)
    ```
    viewDiv(定义的地图div)
    │
    └───viewDiv_root
        └───viewDiv_container
            └───viewDiv_layers(存放地图的div)
    ```
    可以看到，在我们没有移动地图时，viewDiv_layers的transform都为0px；而这时候转化的canvas是并没有偏移的。
    ![没有偏移](/img/arcgis-export/4.png)
    但是，当我们拖动地图的时候，viewDiv_layers的transform开始变化。所以，html2canvas并没有将transform渲染上去。
    ![变化](/img/arcgis-export/5.png)

    **我们可以在导出前重新加载地图，来去除偏移**
      > 因为代码结合了较多的业务，所以放代码也没有多大的意义，就放伪代码吧。个人也懒的重新写一个。 

      1. 保存当前地图中心点位置以及缩放（就是拖动地图之后的中心点位置以及缩放）
        ```javascript
        var center = this.map.extent.getCenter();
        var zoom = this.map.getZoom();
        ```

      2. 使用上一次的中心点和缩放，重新new一遍地图。
        ```javascript
        const esriMap = new EsriMap("viewDiv", {
            center: center,
            zoom: zoom,
        });
        ```
      3. 这时候，就可以看到transform都变为0了。就可以放心导出了。
  2. 问题2：当文件过大，下载的时候会出现。**失败,网络错误的提示**。
    > 这里有一个[相关答案](https://stackoverflow.com/questions/37135417/download-canvas-as-png-in-fabric-js-giving-network-error/)
    
    这时候不用担心，不是代码有问题，也不是html2canvas问题。上面，我们是用base64来进行下载的，而谷歌浏览器限制了donwload属性的a标签url长度。**这时候我们可以将html2canvas转化为blob，再使用一个插件： [FileSaver](https://github.com/eligrey/FileSaver.js/)进行下载** ，代码如下，只需要在返回canvas的代码块中修改一下就可以了：
    ```javascript
      canvasPrint=function () {
      var targetDom = $("#map");
      /* 克隆截图区域 */
      var copyDom = targetDom.clone();
      copyDom.width(targetDom.width() + "px");
      copyDom.height(targetDom.height() + "px");
      copyDom.attr("id", "copyDom");
      $("body").append(copyDom);
      /* 移除不需要截图的区域 */
      $(".base-map").remove();
      var pathName = document.location.pathname;
      var ctxPath = pathName.substring(1, pathName.substr(1).indexOf('/') + 1);
      html2canvas(copyDom[0], {
          useCORS: true,
          imageTimeout:0
          /* , proxy: "/" + ctxPath + "/proxy/proxyScreenShot" */
      }).then(function (canvas) {
          /* --------------修改部分----------------------- */
          canvas.toBlob(function(blob) {
              FileSaver.saveAs(blob, "image.png");
          });
          /* --------------修改部分----------------------- */
          /* 克隆DOM删除 */
          copyDom.remove();
      });
    }
    ```
  3. 问题3：**无法导出featureLayer的图层**。如图，我们需要出现的房子并没有出现：
    ![1](/img/arcgis-export/8.png)
    为什么呢？我们再来看看各个图层在dom是如何构成：
    ![1](/img/arcgis-export/9.png)
    以上，可以看到要素图层是存放于svg里面，而每一个graphic都存放于image标签内。这样就构建成了一个要素图层。
    所以，这有什么关系呢？你可能需要更新一下html2canvas,因为从[0.5.0-alpha1](https://github.com/niklasvh/html2canvas/releases/tag/0.5.0-alpha1)版本才开始支持svg渲染，而且html2canvas会忽略[svg元素](https://stackoverflow.com/questions/40969900/html2canvas-ignores-my-svg-elements/51056005#51056005)，不过只需要添加一下 **allowTaint: true** 属性就可以了。
    ```javascript
      canvasPrint=function () {
      var targetDom = $("#map");
      /* 克隆截图区域 */
      var copyDom = targetDom.clone();
      copyDom.width(targetDom.width() + "px");
      copyDom.height(targetDom.height() + "px");
      copyDom.attr("id", "copyDom");
      $("body").append(copyDom);
      /* 移除不需要截图的区域 */
      $(".base-map").remove();
      var pathName = document.location.pathname;
      var ctxPath = pathName.substring(1, pathName.substr(1).indexOf('/') + 1);
      html2canvas(copyDom[0], {
          useCORS: true,
          imageTimeout:0,
          /* --------------修改部分----------------------- */
          allowTaint: true
          /* --------------修改部分----------------------- */
          /* , proxy: "/" + ctxPath + "/proxy/proxyScreenShot" */
      }).then(function (canvas) {
          canvas.toBlob(function(blob) {
              FileSaver.saveAs(blob, "image.png");
          });
          /* 克隆DOM删除 */
          copyDom.remove();
      });
    }
    ```

    以上允许了渲染svg，如果没有意外，下载的时候会出现以下错误:
    ```
    Uncaught DOMException: Failed to execute 'toDataURL' on 'HTMLCanvasElement': Tainted canvases may not be exported.
    ```

    看html2canvas源码发现，貌似如果开启了支持svg，会执行以下代码。
    ```javascript
    var testSVG = function testSVG(document) {
    var img = new Image();
    var canvas = document.createElement('canvas');
    var ctx = canvas.getContext('2d');
    img.src = 'data:image/svg+xml,<svg xmlns=\'http://www.w3.org/2000/svg\'></svg>';

    try {
        ctx.drawImage(img, 0, 0);
        canvas.toDataURL();
    } catch (e) {
        return false;
    }
      return true;
    };
    ```

    就是将svg拿到然后drawImage将svg图片绘制进canvas。这里，如果new的img标签没有设置 **crossOrigin** 属性为 **anonymous** 那么[谷歌浏览器会不允许执行canvas.toDataURL()和toBlob()方法](https://stackoverflow.com/questions/20424279/canvas-todataurl-securityerror)。因为画布被污染了。

    解决思路:
    > 我觉可以将image标签添设置 **crossOrigin** 属性为 **anonymous** 就可以导出咯。但是这些都是arcgis生成的。我并没有试过。或者可以修改html2canvas的源码？不过，因为思维局限，我想到了另外一种方法：将svg部分转化为image->再将image写入到canvas

    1. 将svg部分转化为image，这里我用了[saveSvgAsPng](https://github.com/exupero/saveSvgAsPng)插件，再将image写入到canvas。
        ```javascript
        canvasPrint=function () {
        var targetDom = $("#map");
        /* 克隆截图区域 */
        var copyDom = targetDom.clone();
        copyDom.width(targetDom.width() + "px");
        copyDom.height(targetDom.height() + "px");
        copyDom.attr("id", "copyDom");
        $("body").append(copyDom);
        /* 移除不需要截图的区域 */
        $(".base-map").remove();

        /* --------------修改部分----------------------- */
        /* 转换svg，找到对应的svg元素，再设置crossOrigin */
        let dom = document.querySelector("#viewDiv_layers svg");
        let img = new Image();
        saveSvgAsPng.svgAsDataUri(dom,{}, (uri:any)=>{
            img.src = uri;
            /* 这里是重点 */
            img.setAttribute("crossOrigin",'anonymous')
        });
        /* --------------修改部分----------------------- */

        var pathName = document.location.pathname;
        var ctxPath = pathName.substring(1, pathName.substr(1).indexOf('/') + 1);
        html2canvas(copyDom[0], {
            useCORS: true,
            imageTimeout:0,
            /* , proxy: "/" + ctxPath + "/proxy/proxyScreenShot" */
        }).then(function (canvas) {

            /* --------------修改部分----------------------- */
            let ctx = canvas.getContext("2d") /* 对应的CanvasRenderingContext2D对象(画笔) */
            ctx.drawImage(img,0,0);  
            /* --------------修改部分----------------------- */

            canvas.toBlob(function(blob) {
                FileSaver.saveAs(blob, "image.png");
            });
            /* 克隆DOM删除 */
            copyDom.remove();
        });
      }
      ```

    以上，就能解决了。
  5. 问题5：如何在地图放大之后，导出包括视野范围外的整张地图。大致步骤：导出前修改dom的宽度和高度->触发地图更新->导出
    导出前进行对地图dom的width和height进行调整。
    明确一下：每一次对地图的zoom，width和height都会*2，所以计算每次zoom是2的（zoom差值的次方）。
    ```JavaScript
    /* 
    zoomOrigin(原始放大级别),zoomChange(当前放大级别)
    widthOrigin(原始放宽度)
    heightOrigin(原始高度)
    关系如下：
    width = widthOrigin * Math.pow(2,(zoomOrigin-zoomChange))
    height = heightOrigin * Math.pow(2,(zoomOrigin-zoomChange))
     */
    var zoomOrigin;
    ...
    var zoomChange = this.map.getZoom();
    var dom = document.getElementById("viewDiv");
    dom.style.width = dom.style.width * Math.pow(2,(zoomOrigin-zoomChange))
    dom.style.width = dom.style.width * Math.pow(2,(zoomOrigin-zoomChange))
    /* dom发生变化之后地图会自动进行调整，这里由于宽度和高度是向右下扩展的，
      所以，我们需要重新定位中心点，这里需要监听map的更新完成事件，再进行中心点调整 */
    ...
    var center;/* 原来记录好的中心点*/
    var updateEvent = this.map.on("update-end",function(event){
      this.map = new EsriMap("viewDiv", {
          center: center,
          zoom: zoomChange,
      });
      updateEvent.remove();/* 移除事件监听 */
    })
    ...
    //之后进行导出即可
    
    ```

  4. 问题4：使用瓦片服务，会出现跨域问题。地图会空白,如图，左边为需要的效果，右边为实际效果。
    ![试试](/img/arcgis-export/12.png)

    一般都会有代理软件吧，所以其实只要把瓦片服务代理一下就可以了。用Apache或者Nginx等都可以。

  ### 总结
  总的来说，如果用“截图”的方式导出。需要使用html2canvas插件，然后再解决偏移、下载、要素图层无法渲染的问题即可。

