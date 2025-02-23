---
title: 【前端】2018年的一些零散而简单的知识点
categories: 前端
tags: 前端
date: 2019.02.01 16:09:18
---
> 下面是我部分在2018年记录的一些知识点
- JavaScript，将一个对象数组的某个属性抽离出来并组成新的数组。使用**map**函数。这个函数十分常用
  ```
  var array  = [{name:"alan",age:18},{name:"jade",age:18}];
  var names = array.map(function(v){return v.name;})
  console.log(names);//["alan","jade"];
  ```
- javascript，如何将一个对象的属性转化成List：
  ```javascript
  //方法一，使用自带的方法
  var keys = Object.keys(myObject);//IE9+
  //方法二，自己实现
  var keys  = function(obj){//自己实现
    var keys = [];
    for(var key in obj){
      keys.push(key);
    }
    return keys;
   }
  ```  

- JavaScript快速获取文件的后缀，这个太巧妙了！
  ```
  var suffix = filename.split('.').pop();
  ```
- Jquery移除style、attribute、class
  ```
  $.css("background-color", "");
  $("selector").removeClass();
  $("selector").addClass();
  $("selector").attr("attr","vl");
  ```
- Javascript中在Click事件之前处理
  >  "mouse up" or "mouse down" event 是在click之前执行的。
  ```
  $("div").mousedown(function(){
  console.log("mouse down event") 
  })
  $("div").mouseup(function(){
  console.log("mouse up event") 
  })
  $("div").click(function(){
  console.log("click event") 
  })
  ```
- [关于鼠标点击事件的触发顺序](https://stackoverflow.com/questions/5497073/how-to-differentiate-single-click-event-and-double-click-event)

| 现代浏览器 |  IE浏览器 |
|:----------:|:---------:|
|  mousedown | mousedown |
|   mouseup  |  mouseup  |
|    click   |   click   |
|  mousedown |  mouseup  |
|   mouseup  |  dblclick |
|    click   |           |
|  dblclick  |           |
  
- JavaScript转义html
  > 有时候html会被服务器拦截。可以将html元素转义之后再发送
演示地址：[http://jsfiddle.net/Daniel_Hug/qPUEX](http://jsfiddle.net/Daniel_Hug/qPUEX)

  ```
  function escapeHTML(html) {
    escape.textContent = html;
    return escape.innerHTML;
  }

  function unescapeHTML(html) {
    escape.innerHTML = html;
    return escape.textContent;
  }
  ```

- JavaScript解析Url
  >  使用A元素的特性
  ```
  var parser = document.createElement('a');
  parser.href = "http://example.com:3000/pathname/?search=test#hash";

  parser.protocol; // => "http:"
  parser.host;     // => "example.com:3000"
  parser.hostname; // => "example.com"
  parser.port;     // => "3000"
  parser.pathname; // => "/pathname/"
  parser.hash;     // => "#hash"
  parser.search;   // => "?search=test"
  parser.origin;   // => "http://example.com:3000"
  
  /**解析url中的查询参数**/
  var parseUrl = function(url){
   var parser = document.createElement('a');
   parser.href = url;
   try {
      if(parser.search){
         var param = parser.search.slice(1,parser.search.length);
         if(param){
            var paramArr = param.split("&");
            if(paramArr){
               parser.param = {};
               paramArr.map(function(v){
                  var vt = v.split("=");
                  parser.param[vt[0]] = vt.length===2?vt[1]:"";
                  return parser.param;
               });
            }
         }
      }
   } catch (error) {
      console.log("解析地址出错！");
   }
   return parser;
  }

  var parser = parseUrl("http://example.com:3000/pathname/?search=test#hash");
  console.log(parser.param) // {search: "test"}
  ```
- 重新加载iframe
  ```
  <script>
    if (window.top.location != window.self.location) {
        top.window.location.href = window.self.location;//在iframe内调这个方法
    }
  </script>
  ```

- Oracle 查询Clob字段是否包含某个字符串
  ```
  SELECT *
  FROM   your_table
  WHERE  DBMS_LOB.INSTR( clob_column, 'string to match' ) > 0;

  --或者
  SELECT *
  FROM   your_table
  WHERE  clob_column LIKE '%string to match%';
  ```
  两者性能差别不大。[我测试过](https://stackoverflow.com/questions/44025050/check-if-clob-contains-string-oracle/53643771#53643771)

- 使用纯css 样式实现fieldset 效果。使用原来的fieldset 标签会出现一些问题，所以就可以使用css直接来实现。
   [Demo地址]([http://jsbin.com/ulema/158/edit](http://jsbin.com/ulema/158/edit)
)
 
  ```
  <style>
  .fieldset {
  border: 2px groove threedface;
  border-top: none;
  padding: 0.5em;
  margin: 1em 2px;
  }

  .fieldset>h1 {
  font: 1em normal;
  margin: -1em -0.5em 0;
  }

  .fieldset>h1>span {
  float: left;
  }

  .fieldset>h1:before {
  border-top: 2px groove threedface;
  content: ' ';
  float: left;
  margin: 0.5em 2px 0 -1px;
  width: 0.75em;
  }

  .fieldset>h1:after {
  border-top: 2px groove threedface;
  content: ' ';
  display: block;
  height: 1.5em;
  left: 2px;
  margin: 0 1px 0 0;
  overflow: hidden;
  position: relative;
  top: 0.5em;
  }
    </style>
  <fieldset>
    <legend>Legend</legend> Fieldset
  </fieldset>

  <div class="fieldset">
    <h1><span>Legend</span></h1> Fieldset
  </div>
  ```

- 获取元素的4个角坐标。**getBoundingClientRect**
  ```
  var rect = element.getBoundingClientRect();
  console.log(rect.top, rect.right, rect.bottom, rect.left);
  ```
- JavaScript里面添加一个css文件
  ```
  function addCss(path){
    var head  = document.getElementsByTagName('head')[0];
    var link  = document.createElement('link');
    link.rel  = 'stylesheet';
    link.type = 'text/css';
    link.href = $appStaticsPath+path;
    link.media = 'all';
    head.appendChild(link);
  }
  addCss('/json/jsoneditor.min.css');
  ```
- 为元素添加阴影的css
  ```
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
  ```
- 使用css为textarea添加行号。[Demo地址](https://jsfiddle.net/vaakash/5TF5h/)
  ```
  textarea
    {
        background: url(http://i.imgur.com/2cOaJ.png);
        background-attachment: local;
        background-repeat: no-repeat;
        padding-left: 35px;
        padding-top: 10px;
        border-color:#ccc;
        line-height:16px;
        font-size: 14px;
    }
  ```
  ![效果](/img/front/29.webp)

- 为超出的添加省略号
  ```
  <div style="width:50px;">test test testtesttesttesttesttest</div>
  <style>
  div {//添加以下样式即可
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  </style>
  ```
- input range（滑动条）实现。
 [https://leaverou.github.io/multirange/](https://leaverou.github.io/multirange/)
[https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/range](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/range)
[https://codepen.io/anon/pen/OBvQgz 支持IE9](https://codepen.io/anon/pen/OBvQgz)

- 纯CSS 实现的时间轴效果
   [https://codepen.io/jessyca27/pen/vGoBex](https://codepen.io/jessyca27/pen/vGoBex)
  [https://www.w3schools.com/howto/howto_css_timeline.asp](https://www.w3schools.com/howto/howto_css_timeline.asp)
![横向和纵向的效果](/img/front/30.webp)

- JavaScript中添加一段Css样式
  ```
  var css = document.createElement("style");
  css.type = "text/css";
  css.innerHTML = 
    ".ace_line span{"+
      "font-size: 13px!important;"+
    "}";
  document.body.appendChild(css);
  ```
- JavaScript为Json格式化。
  ```
  var str = JSON.stringify(obj, null, 2); // 这样就会帮你制表符为2个空格的格式化了
  ```
- JavaScript为Json语法高亮。输出为html。[效果地址](http://jsfiddle.net/KJQ9K/554/)
  ```
  function syntaxHighlight(json) {
    if (typeof json != 'string') {
         json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
  }
  ```
- 很多时候页面的元素排版错误（出现于ElementUI，Layui）。可能是在隐藏的时候，程序没有判断好各个元素的宽高。一般只需要触发**resize**事件就可以让排版恢复正常了。
  ```
    window.dispatchEvent(new Event("resize"));
  ```
- **nodeName**为Jquery保留的属性。如果你为一个元素的name属性赋值为**nodeName**会报错。
  ```
  <input type="text" id="nodeName"  name="nodeName"  /> //这样使用jquery的时候会报错
  //错误为：uncaught-typeerror-elem-nodename-tolowercase-is-not-a-function-jquery
  ```
- 判断一个元素是否含有某个属性。分别使用document原生的方法：**hasAttribute**判断，或者使用Jquery的attr来判断
  ```
  //原生方法判断
  element.hasAttribute("name");
  $(selector)[0].hasAttribute("name");
  
  //jquery方法判断
  var attr = $(this).attr('name');
  // For some browsers, `attr` is undefined; 
  //for others, `attr` is false. Check for both.
  if (typeof attr !== typeof undefined && attr !== false) {
   // Element has this attribute
  }
  ```
- 获取CheckBod的选中的值
  ```
  var hobbies = [];
  var checkeds = $("input[name=hobby]:checked")//获取所有的选中的元素
  $.each(checkeds ,funcition(){ hobbies.push($(this).val()); })
  
  ```
- Jquery为动态元素添加监听。（元素是后来拼接上去的）
  ```
  $(document).on("click", ".your-class", function() {
   //do something
  });
  ```
- Jquery判断一个元素是否显示/隐藏。
  ```
  $(element).is(":visible"); //是否显示
  $(element).is(":hidden"); //是否隐藏
  ```
- Jquery选择包含某个属性但是属性值不等于某个值的选择器。
  ```
  $("ul").find("[class][class!='A']").css("color", "red");
  //选择包含class属性但是class不为A的元素
  ```
- Java，在当前日期上添加一天（获取明天这个时间的日期）。
  ```
  Date dt = new Date();
  Calendar c = Calendar.getInstance(); 
  c.setTime(dt); 
  c.add(Calendar.DATE, 1);
  dt = c.getTime();
  ```
- Java中，将获得的日期置为当天的零点
  ```
  public class Main
  {
   static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args)
    {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        System.out.println(sdf.format(now.getTime()));
        now.set(Calendar.HOUR_OF_DAY, 0);
        System.out.println(sdf.format(now.getTime()));
    }
  }
  ```

- Java中，判断是否为同一天（只判断日期，不判断精确的时间）。
  ```
  import org.apache.commons.lang.time.DateUtils;

  DateUtils.isSameDay(date1, date2);
  ```
- Java中，[判断字符串的大小（可以用于排序）]([https://stackoverflow.com/questions/5153496/how-can-i-compare-two-strings-in-java-and-define-which-of-them-is-smaller-than-t](https://stackoverflow.com/questions/5153496/how-can-i-compare-two-strings-in-java-and-define-which-of-them-is-smaller-than-t)
)
  ```
  str1.compareTo(str2);
  //例子
  "a".compareTo("b"); // returns a negative number, here -1
  "a".compareTo("a"); // returns  0
  "b".compareTo("a"); // returns a positive number, here 1
  ```
- Java中，将Array转化为Set
  ```
  Set<T> mySet = new HashSet<T>(Arrays.asList(someArray));
  ```
- Java中，将List转化为数组（array）
  ```
  String [] countries = list.toArray(new String[list.size()]);
  ```
- [Java里面Map转化为对应的对象]([https://stackoverflow.com/questions/16428817/convert-a-mapstring-string-to-a-pojo](https://stackoverflow.com/questions/16428817/convert-a-mapstring-string-to-a-pojo)
)
  ```
  import com.fasterxml.jackson.databind.ObjectMapper;
  final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
  final MyPojo pojo = mapper.convertValue(map, MyPojo.class);
  ```
- [Java里面对象转化为Map]([https://www.mkyong.com/java/java-convert-object-to-map-example/](https://www.mkyong.com/java/java-convert-object-to-map-example/)
)
  ```
  import com.fasterxml.jackson.databind.ObjectMapper;
        ObjectMapper oMapper = new ObjectMapper();

        Student obj = new Student();
        obj.setName("mkyong");
        obj.setAge(34);

        // object -> Map
        Map<String, Object> map = oMapper.convertValue(obj, Map.class);
        System.out.println(map);//
  
  ```
- Java中，将Json字符串转化为**List<Map<String,Objet>>**结构
  ```
  import com.fasterxml.jackson.databind.ObjectMapper;
  List<Map<String,Objet>> result = new ObjectMapper().readValue(json,  new TypeReference<List<HashMap<String,Object>>>() {});
  ```
- Java中，将Map转换为Json字符串
  ```
  import com.fasterxml.jackson.databind.ObjectMapper;
  String json =new ObjectMapper().writeValueAsString(map);
  ```
- Java中，使用占位符替换String的字符串。
  ```
  //按顺序替换
  String st = "Hello, I'm %s . She is %s "; 
  String result = String.format(st, "Alan", "Jade");
  //结果：Hello, I'm Alan . She is Jade

  //按顺序替换按位置替换
  String st = "Hello, I'm %1$s . She is %2$s "; 
  String result = String.format(st, "First Val", "Second Val");
  //结果：Hello, I'm Alan . She is Jade
  ```
- Java的集合对象使用join
  ```
  import org.apache.commons.lang.StringUtils
  StringUtils.join(ids, ",");
  ```
- Java获取本机的Ip地址
  ```
  import java.net.InetAddress;
 
  public class Main {
   public static void main(String[] args) 
   throws Exception {
      InetAddress addr = InetAddress.getLocalHost();
      System.out.println("Local HostAddress: 
      "+addr.getHostAddress());
      String hostname = addr.getHostName();
      System.out.println("Local host name: "+hostname);
   }
  }
  ```
- 导出Visual Studio Code 的插件列表，并执行安装这些插件
  ```
  //在控制台中执行以下命令：
  code --list-extensions | xargs -L 1 echo code --install-extension
    ```
  //导出之后大概是这样
  ```
  code --install-extension Angular.ng-template
  code --install-extension DSKWRK.vscode-generate-getter-setter
  code --install-extension EditorConfig.EditorConfig
  code --install-extension HookyQR.beautify
  ```
  //然后直接复制到另外一台机子的VsCode中，执行就可以了

