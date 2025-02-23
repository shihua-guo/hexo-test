---
title: 【后端】基于NodeJS的简易DDNS
categories: 后端
tags: 后端
date: 2019.04.27 11:16:07
---
> 无意间看到腾讯云的[API文档]([https://cloud.tencent.com/document/product/302/8516](https://cloud.tencent.com/document/product/302/8516)
)，发现提供修改解析记录的接口。然后在想能否搭建一个非常简易的小程序，用于修改域名的解析记录呢？经过试验，是没问题的。[文章的所有的代码]([https://github.com/shihua-guo/node-ddns](https://github.com/shihua-guo/node-ddns)
)
#### 思路
- 使用NodeJS编写一个简易的服务端，验证来自于客户端的请求之后，然后调用腾讯的api将对应的三级域名的解析修改成最新的IP。
- 简易的流程图如下：![请求的流程图](/img/java/48.png)
- **实现思路。**原来，我想法很简单，就直接在路由器使用脚本发起修改解析的请求。后来，我想可以做一个服务端，用于响应多个客户端的请求，可以添加或者修改解析记录，这样就可以用于多台的设备了。
- **校验请求。**既然多个客户端，那么校验是需要的，所以可以使用静态文件配置token对应3级域名，或者存放与数据库中，恰好，我原本就安装了Mysql数据库，所以我就基于Mysql来存放我的配置内容吧。然后下面就是使用nodejs实现的过程了。

#### 代码实现
> ！！如果你嫌麻烦，可以直接**跳转代码地址。**。[github]([https://github.com/shihua-guo/node-ddns](https://github.com/shihua-guo/node-ddns)
)
##### 调试腾讯云API
  - 查看了腾讯云的API文档，发现不仅仅简单的发送参数和SecretId和SecretKey就可以了完成请求了，api的[公共参数]([https://cloud.tencent.com/document/api/377/4153](https://cloud.tencent.com/document/api/377/4153)
)还需要一个[签名]([https://cloud.tencent.com/document/product/215/1693](https://cloud.tencent.com/document/product/215/1693)
)，所以我们需要先计算出每一次请求的签名值。我也没找到官方提供计算签名的工具或者代码，所以自己在浏览器的控制台实现吧。
- 计算签名。这个过程遇到了比较多的问题，因为我并没有非常仔细的[阅读文档](https://cloud.tencent.com/document/product/215/1693)。下面是签名的算法：
> 代码注释里面写了注意的事项，都是有可能造成调用api失败。
```javascript
/**
* 注意：
* 1. 算的签名必须要经过encodeURIComponent编码。
* 2. 检查请求的地址有没有错
* 3. 检查请求的action有没有错
* 4. 检查时间戳有没有过期
* 5. 默认为Get请求，请使用get请求。使用encodeSignature作为Signature参数
* 6. 排序的时候大小写敏感了。使用原生的排序即可
* 7. 官网例子：parseUrl("https://cns.api.qcloud.com/v2/index.php?Action=DescribeInstances&InstanceIds.0=ins-09dx96dg&Nonce=11886&Region=ap-guangzhou&SecretId=XXXX&SignatureMethod=HmacSHA256&Timestamp=1465185768","Gu5t9xGARNpq86cd98joQYCN3Cozk1qA",[]);、
    返回：0EEm/HtGRr/VJXTAD9tYMth1Bzm3lLHz5RCDv1GdM8s=
    编码：0EEm%2FHtGRr%2FVJXTAD9tYMth1Bzm3lLHz5RCDv1GdM8s%3D
* @param {*} url 除了Signature参数以外的get请求字符串。如：https://cns.api.qcloud.com/v2/index.php?Action=DescribeInstances&InstanceIds.0=ins-09dx96dg&Nonce=11886&Region=ap-guangzhou&SecretId=XXX&SignatureMethod=HmacSHA256&Timestamp=1465185768
* @param {*} key 你的SecretId
* @param {*} result 传入一个数组。因为这里只有一个方法，需要请求加密的js，是异步的，不能将结果直接返回给你。通过数组将结果传递出去
* @returns 返回一个对象：
    {
        encodeSignature --编码后的签名（你需要的是这个）
        Signature --计算的签名
        href --你传递的地址
        param --解析出来的查询参数
        paramKeys --解析出来的查询参数的key
        paramSort --参数的排序（原生的js排序）
        paramJoins --参数的字符串
        paramJoin --
        joinAllGet --排序后的拼接请求字符串
    }
*/
function parseUrl(url, key, resultArr) {
    var addressConfig = {
        "RecordCreate": "cns.api.qcloud.com",//添加解析记录
        "RecordStatus": "cns.api.qcloud.com",//设置解析记录状态
        "RecordModify": "cns.api.qcloud.com",//修改解析记录
        "RecordList": "cns.api.qcloud.com",//获取解析记录列表
        "RecordDelete": "cns.api.qcloud.com",//删除解析记录
        "DescribeInstances": "cvm.api.qcloud.com"//查看实例列表
    };
    function _parser(url) {
        var result = {};
        var parser = document.createElement('a');
        parser.href = result.href = url;
        try {
            if (parser.search) {
                var param = parser.search.slice(1, parser.search.length);
                if (param) {
                    var paramArr = param.split("&");
                    if (paramArr) {
                        parser.param = result.param = {};
                        parser.paramKeys = result.paramKeys = paramArr.map(function (v) {
                            var vt = v.split("=");
                            parser.param[vt[0]] = result.param[vt[0]]   = vt.length === 2 ? vt[1] : "";
                            return vt[0];
                        });
                        parser.paramSort = result.paramSort = {};
                        /*1. 对参数排序 需要忽略大小写*/
                        parser.paramJoins = result.paramJoins = parser.paramKeys.sort().map(function (v) {
                            var value = decodeURIComponent(parser.param[v]);
                            parser.paramSort[v] = result.paramSort[v] = value;
                            return v + "=" + value;
                        });
                        /*2. 拼接请求字符串*/
                        parser.paramJoin = result.paramJoin = parser.paramJoins.join("&");
                        /*3. 拼接签名原文字符串 请求方法 + 请求主机 +请求路径 + ? + 请求字符串*/
                        parser.joinAllGet = result.joinAllGet = "GET" + (addressConfig[parser.param.Action]) + "/v2/index.php?" + parser.paramJoin;
                        /*4. 生成签名串*/
                        var hash = CryptoJS.HmacSHA256(parser.joinAllGet, key);
                        var hashInBase64 = CryptoJS.enc.Base64.stringify(hash);
                        parser.Signature = result.Signature = hashInBase64;
                        parser.encodeSignature = result.encodeSignature = encodeURIComponent(hashInBase64);
                    }
                }
            }
        } catch (error) {
            console.log("解析地址出错！");
            console.log(error);
        }
        return result;
    }
    $.getScript("https://cdnjs.cloudflare.com/ajax/libs/crypto-js/3.1.2/rollups/hmac-sha256.js", function () {
        $.getScript("https://cdnjs.cloudflare.com/ajax/libs/crypto-js/3.1.2/components/enc-base64-min.js", function () {
            var _result = _parser(url);
            resultArr.push(_result);
            console.log(resultArr);
        });
    });
}
```
**如何运行这段代码呢？很简单，打开浏览器的控制台，整段复制进去，回车，会产生一个全局的方法：parseUrl。然后再在控制台调用parseUrl，参数在代码注释里面说的很清楚了，也有例子，返回的对象的encodeSignature属性就是你最终的签名值了**
使用上述代码计算出签名值，然后再调用腾讯api，看看能否调试通过。通过就可以进行服务端的编写了。

##### 服务端编码
> 我也很少接触nodejs。所以也写详细一点，也当做是自己的学习笔记。准备环境有：nodejs，mysql数据库（非必须，你可以把配置以及验证信息放到一个配置文件里面）。
- **初始化项目。**建立文件夹，**node-ddns**。运行：**npm init**。按照常规的输入内容。入口为**app.js**。
- **添加依赖**。根据我们需求，需要2个依赖：**express（用于处理请求）、mysql（用于连接、查询mysql数据库）。**运行命令：
  ```
  npm install express --save
  npm install mysql --save
  ```
  以上完成后，package.json文件大概是以下内容：
  ```
  {
  "name": "node-ddns",
  "version": "1.0.0",
  "description": "",
  "main": "app.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "author": "alan",
  "license": "ISC",
  "dependencies": {
    "express": "^4.16.4",
    "mysql": "^2.17.1"
  }
  }
  ```
  稍等片刻，就可以安装完毕了。**如果出现错误，可以尝试使用管理员运行以上命令**
 - **编写测试接口，连接mysql数据库。**我们在app.js可以写下代码了。引入express和mysql依赖。非常简单，内容如下，意思是程序运行在3000端口，并且有一个/test的接口，返回**“测试成功”**字样：
  ```
  var express = require('express');
var mysql = require("mysql");
var app = express();
var connection = mysql.createConnection({
    "host" : "111.230.165.16",
    "user" : "root",
    "password" : "alan@MYSQL!@#",
    "database" : "test"
});
connection.connect(function(err){
    if(err){
        console.log(err);
    }
});
app.get('/test',function(req,resp){
    connection.query("select 1 from dual",function(error,results,fields){
        if(error){
            resp.send('测试失败');
        }else{
            resp.send('测试成功');
        }
    });
});
app.listen(3000);
console.log("running on 3000");
  ```
  运行命令：**node app，**应用启动，然后再浏览器访问，就可以看到以下内容
![image.png](/img/java/49.png)
##### 建立表结构。
> 这里是表结构创建的[语句]([https://github.com/shihua-guo/node-ddns/tree/master/src/sql](https://github.com/shihua-guo/node-ddns/tree/master/src/sql)
)

![表结构用于存放验证信息（ddns_user）和腾讯的token。](/img/java/50.png)
##### 创建工具类

##### 创建API文件

