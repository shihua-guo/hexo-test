---
title: 【arcgis】基于事件实现Arcgis绘制功能
categories: 前端
tags: arcgis
date: 2019.01.15 19:26:06
pin: true
---
> Arcgis JavaScript Api 3.23、Typescript

### 背景
需要做一个地图小工具（测距、侧面积、缓冲区分析），我尝试了Arcgis 自带的绘制工具，但由于图层比较复杂，并且自带的工具几乎不能定制。我就打算使用基本的事件去实现绘制**点线面**的工具。大体的步骤为：分析**点线面**的绘制过程，然后监听鼠标事件，再进行绘制。因为是半年前的事情了，所以就简单重现一下。以下主要提供思路参考(**千万不要想直接复制代码就能运行**)。

#### 分析点、线、面的绘制过程
- 点：点很简单。就是监听鼠标事件，然后获取鼠标的坐标，然后再绘制就可以了。
![绘制点的动作](/img/arcgis-export/draw-1.webp)

- 圆：圆的绘制和点的差不多，也是监听鼠标单击事件，然后获取鼠标的坐标，再根据半径绘制圆。
![圆的绘制动作](/img/arcgis-export/draw-2.webp)

- 线：单击地图开始绘制，从起始点，线段跟随鼠标移动，再此单击鼠标，绘制2个端点的线，线段从新的起点继续跟随鼠标移动。直到双击，绘制多段线结束。
![线段绘制的动作](/img/arcgis-export/draw-3.webp)

- 面：单击地图开始绘制，从起始点，线段跟随鼠标移动，再此单击鼠标，绘制2个短点的线，线段从新的起点继续跟随鼠标移动，首尾相接，同时出现一个多边形，并随着鼠标，多边形的顶点不断变化。直到双击，绘制面结束。
![面绘制的动作](/img/arcgis-export/draw-4.webp)

### 实现过程
> **重点！我一开始是直接监听map的事件，但是发现这样做在切换底图的时候，监听事件会出现问题（我遇到好几个问题，但是具体的我也没有做记录）。所以，我直接监听了地图DIV的原始dom的事件，放心监听的时间类型是：**esri.AGSMouseEvent**，所以会见地理信息都带过来。**
####准备工作，写辅助的方法
- 先写一个工具类：**Utils.ts**，然后写添加/移除事件的方法，为了**兼容IE**，直接使用最普通的监听也是可以的、写将**点集合转化成线/面集合**的二维数组。
```typescript
  /**  
     *@description 事件绑定，兼容各浏览器  
     * @param target 事件触发对象   
     * @param type   事件  
     * @param func   事件处理函数  
     */  
    static addEvents(target:HTMLElement, type:string, func:any) {  
        if (target.addEventListener)    //非ie 和ie9  
            target.addEventListener(type, func, false);  
        else if ((target as any).attachEvent)   //ie6到ie8  
            (target as any).attachEvent("on" + type, func);  
        else target["on" + type] = func;   //ie5  
    };

/**  为了不影响地图自身的事件，所以我们需要在操作完成之后移除所有的绘制事件
     * @description 事件移除，兼容各浏览器  
     * @param target 事件触发对象  
     * @param type   事件  
     * @param func   事件处理函数  
     */  
    static removeEvents(target:HTMLElement, type:string, func:any){  
        if (target.removeEventListener)  
            target.removeEventListener(type, func, false);  
        else if ((target as any).detachEvent)  
            (target as any).detachEvent("on" + type, func);  
        else target["on" + type] = null;  
    }; 

  /**
     * @param pts 将多个点数组，解析成线段的点集合。
     * @returns ptArr 返回点的二维数组
     */
  public static buildCoordinates(pts:Point[]){
      let ptArr:number[][] = [];
      if(pts){
          pts.forEach(pt => {
              ptArr.push([pt.x,pt.y])
          });
      }
      return ptArr;
  }

 /**
   * 返回一个通用的面
   */
public static getComFillSymbol(){
    return new SimpleFillSymbol(SimpleFillSymbol.STYLE_SOLID,new SimpleLineSymbol(SimpleLineSymbol.STYLE_SOLID, new Color("red"), 2),new Color([30,30,30, 0.5]));
  }
```
- 添加一个专门存放**绘制临时图形**的图层，因为**绘制线/多边形**的过程会不断的**清除图层**。这个图层不是**常驻图层**，所以能直接**全部清除**，方便操作
```javascript
  drawEntity:{
        layer?:GraphicsLayer;
        geometry?:Geometry;//存放临时图形
    } = {
        layer:new GraphicsLayer({id:"buffer-draw",opacity: 1})
    }
```
#### 绘制过程。监听各种事件
- **点、圆**的绘制，调用以上的addEvents方法，添加对地图div的**“click”**事件，然后在点击事件里面（arcgis官方封装过有传递坐标信息），根据事件传递过来的**坐标信息**绘制相应的点符号即可：
```typescript
    /**  
     * @param viewDiv 为地图的div   
     * @param toolboxLayer   将图形绘制的图层  
     * @param func   事件处理函数  
     */  
Utils.addEvents(this.viewDiv,"click",function(event: Event){
  let point = (event as esri.AGSMouseEvent).mapPoint;
  let circle = new Circle({
      center: point,
      radius: 10,
      radiusUnit: "esriKilometers"
  });
  let symbol = new SimpleFillSymbol(SimpleFillSymbol.STYLE_SOLID,new SimpleLineSymbol(SimpleLineSymbol.STYLE_SOLID, new Color("red"), 2),new Color([30,30,30, 0.5]));
  let graphic = new Graphic(circle, symbol);
  this.toolboxLayer.add(graphic);
})

```
> 下面是绘制圆的代码，绘制点的，只需要把circle替换成点就可以了。其他不变

```javascript
let point = (event as esri.AGSMouseEvent).mapPoint;
let symbol = new PictureMarkerSymbol({
    "url":this.pinConfig.pic.url,//图片地址
    "height":20,
    "width":13,
    "type":"esriPMS",
});
let graphic = new Graphic(point, symbol);
this.toolboxLayer.add(graphic);
```

------
- 线的绘制：线的过程需要监听3个事件：**点击（开始）、移动（过程）、双击（结束）**。
   -监听单击事件，绘制一个点，并开始绘制和记录之后的所有点击的坐标：
```typescript
    /**
     * @param point 添加的点
     * @prop clickPoints 存储点的变量
     */
  addClickPoint(point:Point){//用于记录画线过程中的所有点坐标，用于后面的绘制多边形
        this.clickPoints.push(point);
        return this.clickPoints.length;
    }
    /**
     * 用于监听点击的方法。
     */
  drawPolylineClick(event: Event){
        let point  (event as esri.AGSMouseEvent).mapPoint;
        let len = this.addClickPoint(point);
        if(len===1){//当第一个点的时候，绘制
            let geometry = new Polyline(ToolUtils.buildCoordinates([point]));
            let graphic = new Graphic(geometry, new SimpleLineSymbol());
            this.toolboxLayer.add(graphic);
        }else{
            let gLen = this.toolboxLayer.graphics.length;
            let geo = ( this.toolboxLayer.graphics[gLen-1].geometry as Polyline).insertPoint(0, len-1, point)
            this.toolboxLayer.graphics[gLen-1].setGeometry(geo);
        }
    }
  /**
     * 在鼠标移动的时候，我们需要不断的监听并绘制线。并将过程的图形存放于drawEntity变量中
     */
    drawPolylineMouseMove(event: Event){
        let point = (event as esri.AGSMouseEvent).mapPoint;
        let len = this.clickPoints.length;
        this.drawEntity.layer.clear();
        if(len>0){
            let lastPt = this.clickPoints[len-1];
            let polyline = new Polyline(ToolUtils.buildCoordinates([lastPt,point]));
            let graphic = new Graphic(polyline, new SimpleLineSymbol());
            this.drawEntity.layer.add(graphic);//这个图层不是常驻图层，所以能直接全部清除，方便操作
        }
    }
/*这个是辅助方法*/
  generateBufferGraphic(geometry:Geometry,distance: number | number[],unit: string | number,){
        let geometryTmp =<Polygon>GeometryEngine.geodesicBuffer(geometry,distance,unit);
        this.BufferLineAnalyze(geometry,this.currentLayers);
        let graphic = new Graphic(geometryTmp, ToolUtils.getComFillSymbol());
        return graphic;
    }
   /**
     * 监听双击事件，已停止绘制
     */
    drawPolylineDblClick(event: Event){
        if(this.clickPoints.length < 1){
            this.$message.warning("请绘制2个以上的点")
            return;
        }
        let point = (event as esri.AGSMouseEvent).mapPoint;
        let len = this.addClickPoint(point);
        let graphics = this.toolboxLayer.graphics
        let gLen = graphics.length;
        let geo = ( graphics[gLen-1].geometry as Polyline).insertPoint(0, len-1, point)
        graphics[gLen-1].setGeometry(geo);
        this.endDrawPolyline();
        let graphic = this.generateBufferGraphic(graphics[gLen-1].geometry,this.BufferConfig.bufferDistance,this.BufferConfig.unit);
        this.BufferLineAnalyze(graphic.geometry,this.currentLayers);
        this.toolboxLayer.remove(graphics[gLen-1])
        this.toolboxLayer.add(graphic);
    }
/**
     * 将上面的事件都添加进来
     */
    drawPolyline(){
        Utils.addEvents(this.viewDiv,"click",this.drawPolylineClick)
        Utils.addEvents(this.viewDiv,"mousemove",this.drawPolylineMouseMove)
        Utils.addEvents(this.viewDiv,"dblclick",this.drawPolylineDblClick)
    }
    /**
     * 双击绘制结束，将所有事件移除
     */
    endDrawPolyline(){
        this.drawEntity.layer.clear();
        Utils.removeEvents(this.viewDiv,"click",this.drawPolylineClick)
        Utils.removeEvents(this.viewDiv,"mousemove",this.drawPolylineMouseMove)
        Utils.removeEvents(this.viewDiv,"dblclick",this.drawPolylineDblClick)
        this.enableAllEvent();
    }
```

------

- 面的绘制：线的过程需要监听3个事件：**点击（开始）**、**移动（过程，需要实时绘制首位相接的面和跟随鼠标移动的线）**、**双击（结束，将所有的点绘制成一个面即可完成）**。
 
```
  /**
     * 监听面的点击。如果是第一个点则初始化面，之后的就直接在该面的基础上追加点
     */
    drawPolygonClick(event: Event){
        let point = (event as esri.AGSMouseEvent).mapPoint;
        let len = this.addClickPoint(point);
        if(len===1){//如果是第一个点则初始化面
            let geometry = new Polygon(ToolUtils.buildCoordinates([point]));
            let graphic = new Graphic(geometry, ToolUtils.getComFillSymbol());
            this.toolboxLayer.add(graphic);
        }else{//之后的就直接在该面的基础上追加点
            let graphics = this.toolboxLayer.graphics
            let gLen = graphics.length
            let geo = (this.toolboxLayer.graphics[gLen-1].geometry as Polygon).insertPoint(0, len-1, point)
            this.toolboxLayer.graphics[gLen-1].setGeometry(geo);
        }
    }
  /**
     * 监听绘制面过程中鼠标的移动。就是不断得更新线和面。不断得将前一个点和鼠标形成的线和面不断绘制
     */
    drawPolygonMouseMove(event: Event){
        let point = (event as esri.AGSMouseEvent).mapPoint;
        let len = this.clickPoints.length;
        this.drawEntity.layer.clear();
        if(len===1){//当只有一个点，那么就只绘制线
            let lastPt = this.clickPoints[0];
            let polyline = new Polyline(ToolUtils.buildCoordinates([lastPt,point]));
            let graphic = new Graphic(polyline, ToolUtils.getComFillSymbol());
            this.drawEntity.layer.add(graphic);
        }else if(len>1){//如果大于2个点，则开始绘制面
            let lastPt = this.clickPoints[len-1];
            let firstPt = this.clickPoints[0];
            let geometry = new Polygon(ToolUtils.buildCoordinates([firstPt,point,lastPt]));
            let graphic = new Graphic(geometry, ToolUtils.getComFillSymbol());
            this.drawEntity.layer.add(graphic);
        }
    }
/**
     * 监听双击，结束绘制
     */
    drawPolygonDblClick(event: Event){
        if(this.clickPoints.length < 2){
            this.$message.warning("请绘制2个以上的点")
            return;
        }
        let point = (event as esri.AGSMouseEvent).mapPoint;
        let len = this.addClickPoint(point);
        let graphics = this.toolboxLayer.graphics
        let gLen = graphics.length
        let geo = (graphics[gLen-1].geometry as Polygon).insertPoint(0, len-1, point)
        graphics[gLen-1].setGeometry(geo);
        this.endDrawPolygon();
    }
/**
     * 开始绘制面，将所有的事件添加
     */
    drawPolygon(){
        let lay = AllEsriMap.findLayer(this.drawEntity.layer.id);
        Utils.addEvents(this.viewDiv,"click",this.drawPolygonClick)
        Utils.addEvents(this.viewDiv,"mousemove",this.drawPolygonMouseMove)
        Utils.addEvents(this.viewDiv,"dblclick",this.drawPolygonDblClick)
    }
    /**
     * 结束绘制面，将所有的事件移除
     */
    endDrawPolygon(){
        this.drawEntity.layer.clear();
        this.clickPoints = [];
        Utils.removeEvents(this.viewDiv,"click",this.drawPolygonClick)
        Utils.removeEvents(this.viewDiv,"mousemove",this.drawPolygonMouseMove)
        Utils.removeEvents(this.viewDiv,"dblclick",this.drawPolygonDblClick)
        this.enableAllEvent();
    }
```
####总结
以上，就是实现的过程。一开始，我还是使用arcgis自带的针对map的点击事件，发现切换底图（地形图，影像图）会出现不能绘制的问题。这个纳闷了很久。所以查了下，监听原生的dom也是可以的。其实所有的都是为了缓冲区分析服务的。自己写工具，可以兼容所有的图层。针对不同的图层（要素图层，自己绘制的图层）都可以进行缓冲区分析。下次有空再写吧