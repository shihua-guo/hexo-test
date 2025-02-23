---
title: 【前端】arcgis绘制功能
date: 2018-06-28 19:47:59
categories: 前端
tags: arcgis
---
## arcgis绘制功能
> 基于api 3.24

### 背景
最近需要做一个地图小工具（测距、侧面积、缓冲区分析）的需求。客户说只要arcgis自带的工具就可以了。然而，项目的图层比较复杂，而且arcgis自带的工具几乎不能定制，所以我并有考虑直接使用自带的工具。所以我的思路如下：
1. 使用arcgis javascript api 的Draw工具（ [**esri/toolbars/draw** ](https://developers.arcgis.com/javascript/3/jsapi/draw-amd.html)），主要功能是绘制出点线面。
2. 然后再计算绘制出来的图形，这里需要使用geometryEngine（ [**esri/geometry/geometryEngine**](https://developers.arcgis.com/javascript/3/jsapi/esri.geometry.geometryengine-amd.html) 3.13才添加的新工具 ），主要功能是计算对应的图形长度面积等。这里也可以使用arcgis server里面的服务来代替。

下面说下实现的步骤：
- 使用Draw工具绘制图形（[官网实例代码](https://developers.arcgis.com/javascript/3/jssamples/#search/Draw)）。
  > new出draw工具--->定义绘制完成的回调函数--->在对应地方添加触发绘制的事件

  1. 引入对应的draw **esri/toolbars/draw** ，定义draw工具
  ```javascript
  var draw = new Draw(map);
  ```
  2. 定义绘制完成的回调函数
  ```javascript
  var draw = new Draw(map);
  /**
    on(type: "draw-complete", listener: (event: { geometry: Geometry; target: Draw }) => void): esri.Handle;
    回调函数的参数有：绘制完成图形的对应的地理信息：geometry，以及整个draw对象。
   */
  draw.on("draw-complete",endDraw)

  function endDraw(event){
    //逻辑处理
  }
  ```

  3. 在对应的dom触发对应的图形绘制，下面举例绘制点的例子。
  ```javascript
  var draw = new Draw(map);
  /**
    on(type: "draw-complete", listener: (event: { geometry: Geometry; target: Draw }) => void): esri.Handle;
    回调函数的参数有：绘制完成图形的对应的地理信息：geometry，以及整个draw对象。
   */
  draw.on("draw-complete",endDraw)

  function endDraw(event){
    //逻辑处理
  }

  var ptDom = document.getElementById("ptDom")
  ptDom.addEventListener("click", drawPoint, false); 
  function drawPoint(){
    this.draw.activate(Draw.POINT);
  }
  ```
    下面分别是开启绘制对应图形的代码，以及取消绘制，可以在对应的dom事件添加：
  ```javascript
  function drawCircle(){//绘制圆
    this.draw.activate(Draw.CIRCLE);
  }
  function drawPolygon(){//绘制多边形
    this.draw.activate(Draw.POLYGON);
  }
  function drawPolyline(){//绘制线
    this.draw.activate(Draw.POLYLINE);
  }
  function endDraw(){
    this.draw.deactivate();
  }
  ```

- 测距、测面积的工具实现。引入工具geometryEngine **esri/geometry/geometryEngine**工具，这里需要在绘制完成的回调参数里面判断绘制的图形类型，然后再做对应处理。下面关注 **endDraw** 函数即可。
  ```javascript
  function endDraw(event){
    //逻辑处理
    var result = null;
    switch (event.geometry.type) {
      case "point":
          
        break;
      case "polyline":
        //使用geodesicLength计算长度
        result = GeometryEngine.geodesicLength(event.geometry,"kilometers");
        break;
      case "polygon":
        //使用geodesicArea计算面积
        result = GeometryEngine.geodesicArea(event.geometry,"kilometers");
        break;

      default:
          break;
    }
  }
  ```
  上面就能得到你需要的结果，然后再你想要的地方显示出来就可以了。

- 缓冲区分析工具实现。我们继续在回调函数 **endDraw** 处理。缓冲区分析，主要就是在绘制的范围内，筛选出该范围内的图形。主要是针对各种图层服务来做对应的处理。下面主要介绍：FeatureServer、以及featureCollection构建的FeatureLayer图层。
  ```javascript
  ...
  var layer //这里是你的各种图层
  ...
  function endDraw(event){
    switch (event.geometry.type) {
      case "polygon":
      //只有是多边形的才进行处理
        switch (layer.type) {
            case "Feature Layer":
                if(layer.url){//如果是基于arcgis server的服务的FeatureLayer
                  /* 引入"esri/tasks/query"，可以直接调用服务的查询，非常方便 */
                  var query = new Query();
                  query.geometry = event.geometry;
                  query.outFields = ["OBJECTID"];
                  var queryTask = new QueryTask(layer.layer.url);
                  var
                  queryTask.execute(query, (results: any) => {
                    //results就是在绘制范围内的要素。
                  });
                }else{//如果是基于featureCollection构建的FeatureLayer

                }
                break;
            default:
                break;
        }
        break;

      default:
          break;
    }
  }
  ```
