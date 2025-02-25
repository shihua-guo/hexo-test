---
title: 【java】Java WSDL 服务调用入门分享
categories: 后端
tags: 后端
date: 2019.10.03 07:39:14
pin: true
---
## Java WSDL 服务调用入门分享

> ~~第一次自己使用代码直接调用webservice。对于这种网上很难找资料，而且找到了不会用，会用但是会报错的代码。内心是非常痛苦的。要静下心来仔细学习webservice也是不可能的，因为非常赶。所以就把我终于学会调用webservice的关键过程写下~~

#### webservice说明

> 此为不专业的说明，仅做了解用。且代码都是从片段里面截取出来的，仅供思路参考，不能直接拿来用。

webservice应该就是想用于跨平台的交互方式，因为在它复杂的xml里面，规定了调用的方式，参数的对象（强类型）等等信息。而WSDL（Web Services Description Language）应该就是一种针对webservice更加详细的约定，因为WSDL描述了webservice的函数、参数、返回值等说明。

#### 如何调用

- ~~自己写调用代码，我不会，只能祈祷大家以后对接能少遇到webservice~~
- **使用CFX生成**。下面我讲一下如何使用**cfx**生成可以直接调用的代码，以及如何调用。

#### CFX生成可直接调用的代码<sup>1</sup>

> 思路如下：下载插件 -> 根据给出的xml生成对应的代码 -> 在代码里面找到main方法 -> 模仿main方法自己去调用 -> 封装让接口更加易用

- **下载cfx的软件。**[**http://mirrors.tuna.tsinghua.edu.cn/apache/cxf/3.3.3/apache-cxf-3.3.3.zip**](http://mirrors.tuna.tsinghua.edu.cn/apache/cxf/3.3.3/apache-cxf-3.3.3.zip)

- **解压后，打开bin文件夹，找到我们需要运行的 wsdl2java**

- 在bin文件夹，打开CMD。输入命令：

  ```bash
  wsdl2java -p 这里填写你的包名 -d 这里填写你生成代码的位置 -client -encoding utf-8 -noAddressBinding  这里填写WSDL的地址
  ```
**注意：使用git bash可能会有以下问题，可尝试使用自带的CMD**：
![image.png](/img/java/20.png)

  **你的包名**：指的是接下来生成的文件会在XX.XX包下面。比如：我填写的是```com.google```，那么生成的文件结构如此：```package com.google.test;```

  **代码的位置**：指的是生成的文件会在XXX路径下面。比如：我填写的是```C:\Users\shihu\Documents\wsdl```，那么生成的文件就会在```C:\Users\shihu\Documents\wsdl```文件下。

  **WSDL的地址**：指的是你将要调用的wsdl的地址，一般就是返回一个xml文件。比如：我填写```http://webservices.amazon.com/AWSECommerceService/AWSECommerceService.wsdl```，那么就是根据该地址返回的xml文件去生成代码。这里我挑选了亚马逊的测试例子。[这里有更多的例子](https://www.cnblogs.com/jianjialin/archive/2009/01/08/1371950.html)，只需要将地址替换即可。

  **所以，完整的命令可以如下：**

  ```bash
  wsdl2java -p com.google -d C:\Users\shihu\Documents\wsdl -client -encoding utf-8 -noAddressBinding  http://webservices.amazon.com/AWSECommerceService/AWSECommerceService.wsdl
  ```

  生成以后的目录结构如下非常多文件，但是别怕[下面会大致说明](#anchor1)<sup>注1</sup>：
  
  ```bash
  C:.
  └─com
      └─google
              Accessories.java  --dom实体
              Arguments.java    --dom实体
              AWSECommerceService.java  --webservice 的service
              AWSECommerceServicePortType.java --可以获取webservice入口的
              AWSECommerceServicePortType_AWSECommerceServicePortCA_Client.java --入口
              AWSECommerceServicePortType_AWSECommerceServicePortCN_Client.java --入口
              AWSECommerceServicePortType_AWSECommerceServicePortDE_Client.java --入口
              AWSECommerceServicePortType_AWSECommerceServicePortES_Client.java --入口
              ...
  ```
  
- **导入并运行代码**

  > 面对这些陌生的代码，我们该如何着手？很简单，找到入口（main方法）就可以。
  - main方法在**Endpoint_Client **结尾的文件里面；里面自动生成了所有服务端提供的方法。我们只需要调用一下main方法就可以帮我们调试所有的接口了。
  - 然后我们模仿main方法，自己将service实例化出来，就可以直接调用服务端接口了。
## 额外产物

#### 将返回的数据转化为json

> 返回之后的是一个dom数据，那么我们如何把他转化为JSON对象呢？

返回的实体大致如下：

```java
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "username",
    "password"
})
@XmlRootElement(name = "login")
public class Login {
    @XmlElementRef(name = "username", namespace = "http://sys.ws.xxx.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> username;
    @XmlElementRef(name = "password", namespace = "http://sys.ws.xxx.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> password;
    ...
```

如果你和我情况类似，那么你可以使用以下方法转化为JSON。我封装了2部分，一个是只转化一个，另外一个是转化集合的。其实本质都是一样的。最主要的代码是```final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e);```

```java
/**
     * 转化xml对象为json
     * @param tClass
     * @param value
     * @param <E>
     * @return
     */
public static <E extends Object> JSONObject convertToObject(Class<E> tClass, E value){
    List<E> list = new ArrayList<E>();
    list.add(value);
    final List<JSONObject> jsonObjects = convertToObject(tClass, list);
    return jsonObjects.isEmpty()?new JSONObject():jsonObjects.get(0);
}
/**
 * 批量转化xml对象为json
 * @param tClass
 * @param list
 * @param <E>
 * @return
 */
public static <E extends Object> List<JSONObject> convertToObject(Class<E> tClass, List<E> list){
	List<JSONObject> result = new ArrayList<>();
	for(E e : list){
		try {
			final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e);
			result.add( JSONObject.parseObject(json));
		} catch (JsonProcessingException ex) {
			logger.error(marker,"解析xml对象错误！",e);
		}
	}
	return result;
}
```

#### 处理SOAP异常

> 只要服务端有异常：包含程序未捕获的异常、参数校验不通过等，都会返回**ServerSOAPFaultException**异常，我们主要是处理**ServerSOAPFaultException**异常。

- 思路：添加一个切面，然后统一捕获**ServerSOAPFaultException**异常。目的：封装一层，将**ServerSOAPFaultException**异常转化为我们自己的业务异常，将SOAP的异常前后缀除去，让他人更容易明白。

- 实现：下面是切面中的around：

  ```java
  @Around(value = "log()")
  public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
  	Object result = null;
  	String argsJson = "";
  	try{
  		Object[] arguments = proceedingJoinPoint.getArgs();//传入的参数
  		argsJson = JSON.toJSONString(arguments);
  	}catch (Exception e){
  	}
  	final String methodName = proceedingJoinPoint.getSignature().getName();
  	try {
  		result = proceedingJoinPoint.proceed();
  	} catch (ServerSOAPFaultException e) {
  		String EXCEPTION_PREFIX = "Client received SOAP Fault from server: ";
  		String EXCEPTION_SUFFIX = " Please see the server log to find more detail regarding exact cause of the failure.";
  		final String message = e.getMessage().replaceFirst(EXCEPTION_PREFIX,"").replaceFirst(EXCEPTION_SUFFIX,"");
  		logger.error("调用接口异常",e);
  		throw new CommonException(message);
  	} catch (Exception e) {
  		logger.error("调用接口异常",e);
  		throw e;
  	}
  	return result;
  }
  ```

  

#### 如何添加头部

> 有时候，我们需要在SOAP头部（服务器端的XML说明）或者http头部添加验证信息（仅仅是请求的http头部）。那么如何做到呢？

- SOAP头部的添加，参考网上的<sup>2</sup>。思路如下：

1. 实现**HandlerResolver** 接口 
  
2. 实现**SOAPHandler<SOAPMessageContext>** 接口 
  
2. 重写**handleMessage**方法。
  
3. **new 一个HandlerResolver，并将实现类放入service**
  
4. 从**service获取endpoint**调用
  
5. 代码：
  
     ```java
     
     /**
      * 实现SOAPHandler
      * @return
      */
     private class RequesterCredentials implements SOAPHandler<SOAPMessageContext> {
       private Map<String,String> headers;
     
       public RequesterCredentials(Map<String,String> headers) {
         super();
         this.headers = headers;
       }
       public Set<QName> getHeaders() {
         return null;
       }
     
       @Override
       public void close(MessageContext context) {
       }
     
       @Override
       public boolean handleFault(SOAPMessageContext context) {
         // TODO return true
         logger.error("ws错误处理。");
         return true;
       }
     
       // 处理请求上下文
       @Override
       public boolean handleMessage(SOAPMessageContext context) {
         try {
           Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
     
           if (outboundProperty.booleanValue()) {
             SOAPMessage message = context.getMessage();
     
             SOAPHeader header = message.getSOAPHeader();
             if (header == null) {
                 message.getSOAPPart().getEnvelope().addHeader();
                 header = message.getSOAPHeader();
             }
             SOAPElement authenticationToken = header.addChildElement("auth", "", "");
             authenticationToken.addChildElement("user").addTextNode("user");
             authenticationToken.addChildElement("password").addTextNode("password");
             authenticationToken.addChildElement("token").addTextNode("token");
           }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return true;
       }
     }
     
     
     /**
      * 实现SOAPHandler
      * @return
      */
     public class HeaderHandlerResolver implements HandlerResolver {
       private RequesterCredentials requesterCredentials;
     
       public HeaderHandlerResolver(RequesterCredentials requesterCredentials){
           this.requesterCredentials=requesterCredentials;
       }
       @Override
       public List getHandlerChain(PortInfo portInfo) {
           return Arrays.asList(requesterCredentials);
       }
     }
   ```
  
     ```java
     /**
      * 放入SOAPHandler，并调用
      * @return
      */
     RequesterCredentials  r=new RequesterCredentials (headers);
     HeaderHandlerResolver  headerHandlerResolver=new HeaderHandlerResolver (r);
     mathService.setHandlerResolver(headerHandlerResolver);
     Deposit1ServicePortType port = mathService.getDeposit1ServiceHttpSoap11Endpoint();
     ```

- **添加http的头部**

  > 和上方的soap差不多。主要就是实现HandlerResolver，重写handleMessage，修改请求的上下文。
  
  - 唯一的区别就是重写的handleMessage不同
  
    ```java
    /**
     * 实现SOAPHandler,一个headers成员变量，到时候放入header。
     * @return
     */
    private class RequesterCredentials implements SOAPHandler<SOAPMessageContext> {
      private Map<String,String> ;
    	...
            
    // 改变一
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
      Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outboundProperty.booleanValue()) {
        Map<String, List<String>> requestHeaders = new HashMap<>();
        for(String key:headers.keySet()){
            requestHeaders.put(key, Arrays.asList(headers.get(key)));
        }
        context.put(MessageContext.HTTP_REQUEST_HEADERS, requestHeaders);
      }
      return true;
    }
        
    ```
  
    ```java
    /**
       * 放入SOAPHandler，并调用
       * @return
       */
    RequesterCredentials  r=new RequesterCredentials (headers);
    HeaderHandlerResolver  headerHandlerResolver=new HeaderHandlerResolver (r);
    mathService.setHandlerResolver(headerHandlerResolver);
    Deposit1ServicePortType port = mathService.getDeposit1ServiceHttpSoap11Endpoint();
    ```
  
    
  
  - **需要注意的是，添加的header是 Map<String, List<String>>，千万不要手快，写成 了Map<String, Object>，网上很多都是这里被坑了。注意value是一个String的list。**

#### 将java对象转化为dom对象工具

> 当我们调用webservice方法的时候，传入的参数并不是普通的java对象，而是dom对象，那么我们如何将java对象转化为dom对象呢？

- 非常需要的注意的大前提：DOM对象的Object属性需要通过ObjectFactory统一create出来。
- 我们这里借助了hutool的反射工具
  ```xml
  <dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>4.6.8</version>
  </dependency>
  ```

- 思路：**我们可以通过观察dom对象结构 =》将java对象对应的属性转化为dom对象需要的属性=》把转化好的属性set进dom对象**

下面我们观察一下CXF 生成的dom对象的Object属性和普通属性get、set方法：

```java
/**
 * 设置username属性的值。
 *
 * @param value
 *     allowed object is
 *     {@link JAXBElement }{@code <}{@link String }{@code >}
 *
 */
public void setUsername(JAXBElement<String> value) {
	this.username = value;
}
...
/**
 * 设置money属性的值。
 *
 * @param value
 *     allowed object is
 *     {@link Double }
 *
 */
public void setMoney(Double value) {
	this.money = value;
}    
```

为什么要把Object类和java基类分开，因为只有Object的子类（像String）才不是普通的set方法，而基类是普通的set方法。

可以看到是一个**JAXBElement**包装了一个普通的java类，所有在**java类和dom类的类型保持一致**的情况下（比如java是String，那么dom也是String），我们可以通过反射，将对应的属性通过get方法，**获取到java对象原本的值**，然后再通过**dom的ObjectFactoryt**的create方法，**组合成dom需要的类型的值**，将转化之后的属性**放入dom对象**中。

代码如下：

```java
/**
 * 可以转化普通java的属性为dom的属性
 * @param objecrFactory 对象工厂实例
 * @param klass soap的类
 * @param property 对应java类的属性名（soap和java的属性名字和类型需要保持一致）
 * @param value 需要设置的值
 * @param <T> 对象工厂类
 * @param <E> soap的类
 * @param <V> 属性的类
 * @return
 */
public static <T,E,V> JAXBElement<V>  invokeSOAP(T objecrFactory, Class<E> klass, String property , V value){
	try{
		final String methodName = String.format("create%s%s", klass.getSimpleName(),
				property.substring(0, 1).toUpperCase() + property.substring(1));
		return ReflectUtil.invoke(objecrFactory,methodName,value);
	}catch (Exception e){
		logger.error("设置属性失败！",e);
		return null;
	}
}

/**
 * 将一个普通java对象转化为dom对象
 * @param entity java对象
 * @param objecrFactory 创建的dom工厂
 * @param soapClass soap对应的对象class
 * @param <T> 实体的类
 * @param <O> dom对象工厂的类
 * @param <E> soap的类
 */
public static <T,O,E> E convertToSOAP(T entity,O objecrFactory, Class<E> soapClass){
 final E soapInstance = ReflectUtil.newInstance(soapClass);
   try{
    final Class<?> entityClass = entity.getClass();
      final Set<String> methodNames = ReflectUtil.getPublicMethodNames(entityClass);
        for(String methodName: methodNames){
          if(methodName.startsWith("get") && !objectMethods.contains(methodName) && ReflectUtil.getMethodByNameIgnoreCase(soapClass, methodName)!=null){//如果是get方法
            final String property = methodName.substring(3, methodName.length());
            Object value = ReflectUtil.invoke(entity, methodName);
            if(value instanceof BigDecimal){//BigDecimal需要转化为double，并且直接设置属性
              value = ((BigDecimal) value).doubleValue();
              ReflectUtil.invoke(soapInstance,"set"+property,value);
              continue;
            }
            if(value!=null){
              ReflectUtil.invoke(soapInstance,"set"+property,invokeSOAP(objecrFactory, soapClass, property, value));
            }
          }
        }
		return soapInstance;
	}catch (Exception e){
		logger.error("批量设置属性失败！",e);
		return soapInstance;
	}
}
```

#### 如何在postman中调试

> 拿到对方接口的第一时间，就是想使用postman进行测试。但是，webservice如何使用postman进行测试呢？

- 物料：谷歌浏览器、postman、wizdler插件、以及一个wsdl网址

- 我们这里可以借助一个谷歌插件对**请求参数**进行生成：**Wizdler**

![插件.jpg](/img/java/21.png)


- 安装插件之后，在谷歌浏览器打开你可以需要**调试的wsdl的网址**。那么插件就会显示出该**wsdl对应的方法**，如下图：

  > 然后点击需要调试的方法就可以生成请求体，其实简单的请求，可以直接通过该插件调试了
  >
![WSDL.jpg](/img/java/22.png)


- 比如，我们就尝试请求这个方法（http://www.webxml.com.cn/WebServices/WeatherWebService.asmx）：直接使用插件，在页面调用效果，如下图：

![请求体.png](/img/java/23.png)


**修改请求体**之后，我们可以直接通过页面对方法进行调用。**点击GO按钮**就可以直接发起请求了。对应的响应如下：

![插件发起的请求.png](/img/java/24.png)

同时，我们可以将请求体复制到postman，然后设置对应的**Content-Type**，以及请求方法，使用post请求就可以在**postman**发起webservice请求了。以这里例子为例，需要POST请求；**body是xml格式**；**Content-Typeapplication/soap+xml; charset="UTF-8"**；就可以发送请求了。

![post请求.png](/img/java/25.png)

#### webservice 网络断开后处理

> webservice有一个奇葩的问题。如果对方的webservice服务端是离线的状态（服务器重启/关闭导致我们无法获取到WSDL的xml文件），或导致我们客户端 “启动的时候” 无法初始化webservice的一些东西，或者当我们客户端运行的时候，服务端重启了，服务端重启完毕之后，webservice的一些实例会变成null，这就导致了我们无法正常使用webservice了。我是如何解决这个问题的呢？

- 为什么要这样处理？因为我在**类初始化**的时候，将webservice的service和port**全部初始化**了，并放入静态变量中，这样效率就高，因为不需要每次都new一次。所以，，事先初始化了webservice相关的，如果网络断开了，那么就会抛出NPE异常了。

  初始化代码大致如下：

  ```java
  @Service
  public class WsService{
      public static LoginService loginService;
      public static LoginServicePortType portType;
      public WsService(){
          loginService = new (new URL("url"), LOGIN_SERVICE_NAME);
          portType = loginService.getLoginServiceHttpSoap11Endpoint();
      }
      ...
  }
  ```

- 思路：加入切面 =》代理webservice的每一个方法 =》每次进入方法前，检测ws相关的类是否为空 =》空则重新初始化

  把原来初始化的代码封装成一个statis方法：

  ```java
  @Service
  public class WsService{
      public static LoginService loginService;
      public static LoginServicePortType portType;
      public WsService(){}
      public static initService(){
          try{//如果启动应用的时候，恰好服务端无法使用，那么会抛出异常，所以需要处理。
              //不能把异常抛出，因为抛出异常会导致应用无法启动。为了保证应用能正常启动，必须处理异常
              //每次进入方法的时候再检查ws对象，这样就不影响应用正常启动了，等服务端正常了，就会自动初始化了
              if(loginService == null){
                  loginService = new (new URL("url"), LOGIN_SERVICE_NAME);
              }
              if(portType == null){
                  portType = loginService.getLoginServiceHttpSoap11Endpoint();
              }            
          }catch(Exception e){
              e.printStackTrace();
          }
      }
  }
  ```

  

  切面类：

  ```java
  import org.aspectj.lang.JoinPoint;
  import org.aspectj.lang.annotation.Aspect;
  import org.aspectj.lang.annotation.Before;
  import org.aspectj.lang.annotation.Pointcut;
  import org.springframework.stereotype.Component;
  
  /**
   * 接口日志
   * @author shihu
   */
  @Aspect
  @Component
  public class WsAspect {
  
      private Logger logger = LoggerFactory.getLogger(WsAspect.class);
      @Pointcut("execution(public * com.google.ws.WsService.*(..))")
      public void cut(){}
      @Before("cut()")
      public void doBefore(JoinPoint joinPoint){
          WsAspect.initService();// 确保每次进入都验证是否为空
      }
  }
  
  ```

#### 总结

因为不熟悉webservice，所以一开始完全就是一头雾水。但是，当CFX自动生成了代码能直接调用之后，整个流程就通了，豁然开朗。但是，之后又为设置header头痛。因为不熟悉，所以完全不知道header还区分soap的header和普通的http的header。而前者，我花了大量的时间去研究，最终都没研究出来（因为服务端压根就没有，所以以后要问清楚，但是完全不懂ws所以也就不存在清不清楚的问题了），最终才得知是设置http的header，这个就简单了，就是处理请求的上下文，在上下文添加header就可以了。

## 另

- **参考资料：**

    > 1. 《cxf生成java客户端 webservice》https://blog.csdn.net/yinkgh/article/details/52472770
    > 2. 《webservice之自定义请求头实现》https://blog.csdn.net/do_bset_yourself/article/details/79561852
    > 3. 《java web service client, adding http headers》https://stackoverflow.com/questions/6666060/java-web-service-client-adding-http-headers
    > 4. 《[常用网络上的webservice地址](https://www.cnblogs.com/jianjialin/archive/2009/01/08/1371950.html)》
    > 5. 《[Eclipse根据wsdl文件自动生成webservice client](https://blog.csdn.net/muyangbin/article/details/77750313)》
    > 6. 《[webservice到底是什么](https://www.jianshu.com/p/49d7997ad3b7)》
    > 7. 《[解析webservice](https://blog.csdn.net/qq_35144470/article/details/78778487)》
    > 8. 《[自己调用webservice方法总结（带请求头SoapHeader）](https://blog.csdn.net/JNSHILANG/article/details/84271669)》