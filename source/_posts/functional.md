---
title: 【前端】Functional Programming 的见解
categories: 前端
tags: 前端
date: 2019.11.23 11:09:51
---
> 仅仅是一篇观后感，写于： 2018-03-26，修改于：2019年11月23日 

​	最近在我违反了 **no mutation** ，就是一个函数里面，修改了传入的变量。导致我在另外一个地方使用该变量的时候，并不知道它已经发生了变化。

​	然后，我突然想起了好几个月前看得一个演讲：[Anjana Vakil: Learning Functional Programming with JavaScript - JSUnconf 2016](https://www.youtube.com/watch?v=e-5obm1G_FY)。演讲稿地址：https://slidr.io/vakila/learning-functional-programming-with-javascript；这里面就有讲什么是 **Functional Programming**。我发现，在JS中，遵循了下面几条原则，可以更好的复用、维护代码。

> 在平时写业务的过程中设计模式是谈不上了，但是Functional Programming恰处处可见。

### What is Functional Programming?

> 什么是Functional Programming

- **A programming paradigm.**
  一种编程的范式。就像**面向过程**、**面向对象**。总的来说，**Function is King**

- **A code style.**
  一种代码的风格。如何去组织你的代码。

- **A mindset.**
  一种思维模式。该使用什么样的方式去解决你的问题？就像你不想去破解一个代码块完整性（内聚），那么你可以加入一个切面，去影响该代码块的执行结果。

- **A sexy, buzz-wordy trend.**

  ~~我不知道啥意思~~

### Why Functional Javascript?

- **Object-oriented in javascript gets tricky.**
  因为在JavaScript中，面向对象往往纠缠不清。就比如this，貌似真的很多时候，this的指向会变化多端。

- **Safer, easier to debug/maintain.**

  更加安全且容易去调试/维护。

- **Established community.**

### How Functional Programming in Javascript?

- **Do everything in function：以函数方式思考**
  	非常简单，就是一个**input -> output**的过程。你只需要简单的把input交给一个function处理，然后它会给你需要的output。就像一种数据的流向。比如以下的例子：

    ###### 以下是非Functional的形式（A）：

    ```javascript
  var name = "Alan";
  var greeting = "Hi,I'm ";
  console.log(greeting+name);
   => "Hi,I'm Alan"
   ```
  
    ###### 以下是Functional的形式（B）：
  
  ```javascript
  function greet(name){  
      return "Hi,I'm "+name;
    }
    greet("alan");
  
  => "Hi,I'm Alan"
  ```
  
  **例子A中：**这种明显就是并行处理方式，并没有function，也没有体现出**输入 -> 处理 -> 输出**的数据流形式；而是定义完greet，然后定义name，然后一起打印。
  
  **例子B中：**是将name交给一个greet函数处理，它会返回拼接一个greet然后返回给你。这明显是非常函数style。
    	
  
- **Use pure function：使用纯正的函数**

  > 使用纯正的函数，去避免一些隐藏的问题。

  ​	在Functional Programming中，我们会遇到一个问题：**函数A**中，改变了输入的内容，然后你在**函数B**中使用该input的时候，发现它已经被改变！然后，也许**函数B**中的执行结果，会因为**函数A**中改变了input而改变。这个就是文章开头提及的情况。这时候，你可能会绞尽脑汁，究竟在哪里改变了它。所以，纯净的function，是不应该去改变输入的内容。你应该在一个function里面拿了输入内容，然后只读取该输入内容，然后处理好，并且得出结果，然后把**output返回**。

  ```javascript
  var name = "alan";
  function greet(){  
      name = "jade";  
      return "Hi,I'm "+name;
  }
  function sayMyName(name){  
      return "Hi,I'm "+name;
  }
  greet();
  sayMyName(name);
  => "Hi,I'm alan "
  ```
  
  同样，以下也不是纯净的function
  
  ```java
  var name = "alan";
  function greet(){  
  	console.log("Hi,I'm "+name);
  }
  => "Hi,I'm alan "
  ```
  
   并没有input，而是直接使用了全局的变量。而且，并没有返回计算的结果。我们需要的是：function帮我们计算并返回结果。而打印并不是function需要做的事情。
  
   正确做法应该如下：function唯一需要做的，就是使用input去计算，然后得出我们需要的output，并将output返回。如下：
  
  ```javascript
  var name = "alan";
  function greet(name){  
      return "Hi,I'm "+name;
  }
  => "Hi,I'm alan "
  ```
  
   总之，一个函数，需要尽可能的纯净。

-  **Use higher-order functions：使用更高阶的函数**

  > functions can be inputs/outputs：函数也能作为输入、输出。

  例子：
  
  ```javascript
  /*一个返回函数的函数*/
  function makeAdjectifier(adjective) {
      return function (string) {
          return adjective + “ ” + string;   
      };
  }
  
  /*使用返回的函数，去修饰一个输入*/
  var coolifier = makeAdjectifier(“cool”);
  coolifier(“conference”);
  
  返回 => “cool conference”
  ```
  
-  **Don’t iterate**

   > 不要迭代，我们有更加好的选择：map、reduce、filter

   通常，我们在处理一些数组/集合会使用迭代。我们都习惯了使用for之类的去循环所有的项，然后进行处理。
   
   但是呢，在function program中，我们有更加高级的做法：map、reduce、filter，一些可以直接调用的函数。下面的一个通过map、reduce制作三明治的图，就能很好解释map、reduce的工作原理。
   
   通常，我们制作一个三明治，需要循环去切原料（for一个黄瓜），然后得到三明治的原材料（list）。不过，function style，使用map，我们只需要提供**切这个function**和**黄瓜**，然后就能返回三明治的原材料。

![mapreduce.jpg](/img/front/33.webp)

​	**map**：就是将一个整体（集合）分割，或者说提取。

​	**reduce**：就是将多个元素进行归集。形成一个整体。

​	**filter**：将不符合条件的元素过滤掉（比如：你不喜欢黄瓜。就可以过滤名称为黄瓜的原材料，这样，你做出来的三明治就没有黄瓜）

- **Avoid mutability：不去改变原始数据**

  > 有时候，我们改变了原始数据（input）可能会导致一些隐藏的问题。

  比如以下例子：

  ```javascript
  var rooms = [“H1”, “H2”, “H3”]; // 我们准备了3间房：H1、H1、H3
  rooms[2] = “H4”; // 发现客人不喜欢H3的房间，于是，直接把原来的H3房间替换成H4
  rooms;
  => ["H1", "H2", "H4"] // 于是H3被改变了
  ```

  以上，我一开始就认为，这个数组里面的元素就是：H1、H1、H3；但是，我们并不知道，在我代码的其他地方，悄悄地将H3元素直接变成H4。于是，我就开始了漫长的bug tracking的过程：为什么在这里是H3，到了那里又变成了H4？于是我就在电脑前以泪洗面。

  一个很简单的方法，我们可以把数据当成不变的，使用一个function来解决：

  ```javascript
  var rooms = [“H1”, “H2”, “H3”];
  Var newRooms = rooms.map(function (rm) { 
   if (rm == “H3”) { return “H4”; }
   else { return rm; }
  });
  newRooms; => ["H1", "H2", "H4"]
  rooms; => ["H1", "H2", "H3"]
  ```

  以上，我们使用一个函数来处理将H3更换为H4的需要。但是，我们并没有改变rooms变量的原始数据，并且，我们得到了我们需要的数据：newRooms。

- **Persistent data structures efficient immutability：复用相同的数据以提高部分数据变化的效率**

  继续沿用上面的例子，如果我们想把H3更换成H4，有以下做法：

  做法1：

  ```javascript
  var rooms = [“H1”, “H2”, “H3”]; // 我们准备了3间房：H1、H1、H3
  rooms[2] = “H4”; // 直接把原来的H3房间替换成H4
  => ["H1", "H2", "H4"] // 于是H3被改变了
  ```

  为了保持**Avoid mutability**原则，我们可以非常简单的复制一份新的数组去改变H3元素：

  ```javascript
  var rooms = [“H1”, “H2”, “H3”]; // 我们准备了3间房：H1、H1、H3
  var newRooms = rooms.slice();
  rooms;
  newRooms;
  => [“H1”, “H2”, “H3”]
  => [“H1”, “H2”, “H4”];
  ```

  很好，我们做到了**Avoid mutability**。但是，数据量一旦变得庞大，我们这个方法就不管用了。所以，我们可以换一种思路，如果，我们能够复用相同的部分，只需要替换需要变化的元素，那么就不会浪费这些不必要的空间了。

  首先，我们可以把数组转化成Tree的结构：


![tree1.jpg](/img/front/34.webp)


  然后，当我们需要**替换节点3**的时候，只需要连接节点4，建立一个新的tree。这样，只需要做一个小小的改动，我们就可以共享结构了。


![tree2.jpg](/img/front/35.webp)

  我们可以使用一个immutable-js https://immutable-js.github.io/immutable-js/ js库，来达到以上效果，而不需要自己去写算法。下面是immutable-js 的演示：

![immutable演示.gif](/img/front/36.webp)


  同样，还有推荐一下function style的库：

  ● Mori (http://swannodette.github.io/mori/)
  ● Immutable.js (https://facebook.github.io/immutable-js/)
  ● Underscore (http://underscorejs.org/)
  ● Lodash (https://lodash.com/)
  ● Ramda (http://ramdajs.com/) 

### 更多的FP教程 

《An introduction to functional programming》by Mary Rose Cook
https://codewords.recurse.com/issues/one/an-introduction-to-functional-programming



### 额外的

> 下面是我写的一些map、reduce的例子

##### JAVA

准备工作，有以下类：

```java
class Person{
    private String name;
    private int age;
    private BigDecimal money;
    ...
}
```

---
##### 循环Object集合

  传统做法：

  ```java
  List<Person> list = new ArrayList<>();
  for(Person p:list){
      names.add(p.getName());
  }
  ```

  foreach做法：

  ```java
  List<Person> list = new ArrayList<>();
  list.stream().forEach(p->{ 
      //do something
  });
  ```

  
---
##### 在一个Object集合中，只抽取Object其中一个属性，形成一个list

  传统做法：

  ```java
  List<Person> list = new ArrayList<>();
  List<String> names = new ArrayList<>();
  ...
  for(Person p:list){
      names.add(p.getName());
  }
  ```

  map的做法：

  ```java
  List<Person> list = new ArrayList<>();
  List<String> names = list.stream().map(Person::getName).collect(Collectors.toList());
  ```
---
##### 在一个Object集合中，我们需要将某个属性作为key，形成一个map

  传统做法：

  ```java
  List<Person> list = new ArrayList<>();
  Map<String,Person> map = new HashMap<>();
  ...
  for(Person p:list){
      map.put(p.getName(),p);
  }
  ```
  map做法
  ```java
  List<Person> list = new ArrayList<>();
  Map<String,Person> map = list.stream()
    .collect(Collectors.toMap(Person::getName, p -> p));
  ```
  
---
##### 在一个Object集合中，我们需要将不符合条件的对象过滤掉

  filter的做法：

  ```java
  // 我们将name不是alan的过滤掉
  List<Person> list = new ArrayList<>();
  List<Person> newList = list.stream().filter(p->{
  	return "alan".equals(p.getName());
  }).collect(Collectors.toList());
  ```

- 在一个Object集合中，我们需要统计某个number类型属性的合计。

  传统做法：

  ```java
  List<Person> list = new ArrayList<>();
  int result = 0;
  for(Person p:list){
      result+=p.getAge();
  }
  ```

  stream做法：

  ```java
  List<Person> list = new ArrayList<>();
  int result = list.stream().collect(Collectors.summingInt(Person::getAge));
  ```

  对于**BigDecimal**，我们还可以这样：

  ```java
  List<Person> list = new ArrayList<>();
  // 先map获得集合，再reduce进行归集。
  BigDecimal result = list.stream()
      .map(Person::getMoney)
      .reduce(new BigDecimal("0"),BigDecimal::add);
  ```

---
##### 延伸以上，对于一个非Object的集合，而是一个map结构<String,Interge>的数据，我们可以使用以下进行统计：

  传统做法：

  ```java
  Map<String,Integer> map = new HashMap<>();
  Integer result = 0;
  for(Map.Entry entry:map.entrySet()){
      result += entry.getValue();
  }
  ```

  map做法：

  ```java
  // 方法1：
  Map<String,Integer> map = new HashMap<>();
  Integer result = map.values().stream()
      .mapToInt(Integer::intValue).sum();
  
  // 方法2：
  Integer result = map.values().stream()
      .collect(Collectors.summingInt(Integer::intValue));
  ```

---
##### 对于需要join一个String类型的集合
 java8做法：
  ```
  List<Person> list = new ArrayList<>();
  // 先map获得集合，再reduce进行归集。
  String result = list.stream()
      .map(Person::getName)
      .collect(Collectors.joining(","));
  // 结果：alan,jade,bob
```
---
##### 根据对象集合的属性去重
[https://stackoverflow.com/questions/29670116/remove-duplicates-from-a-list-of-objects-based-on-property-in-java-8](https://stackoverflow.com/questions/29670116/remove-duplicates-from-a-list-of-objects-based-on-property-in-java-8)
 有时候，我们需要将一个对象集合里面的指定属性进行去重，可以这样做：
```
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

...
List<Employee> unique = employee.stream()
                                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparingInt(Employee::getId))),
                                                           ArrayList::new));
```
例子：
```
List<Employee> employee = Arrays.asList(new Employee(1, "John"), new Employee(1, "Bob"), new Employee(2, "Alice"));
那么会根据ID过滤，输出结果：
[Employee{id=1, name='John'}, Employee{id=2, name='Alice'}]
```
---
##### 归集List中的List
[https://stackoverflow.com/questions/25147094/how-can-i-turn-a-list-of-lists-into-a-list-in-java-8](https://stackoverflow.com/questions/25147094/how-can-i-turn-a-list-of-lists-into-a-list-in-java-8)
有时候，我们需要将List里面的List的层次结构转化为平行的结构：
```
方法一：
List<List<Object>> list = ...
List<Object> flat = 
    list.stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());

方法二：
List<List<Object>> listOfList = ... // fill
List<Object> collect = 
      listOfList.stream()
                .collect(ArrayList::new, List::addAll, List::addAll);
```
---
##### 只要集合中符合任意条件，则执行
来源：[https://stackoverflow.com/questions/28596790/throw-an-exception-if-an-optional-is-present](https://stackoverflow.com/questions/28596790/throw-an-exception-if-an-optional-is-present)
有时候，我们需要做这样的业务，当集合中出现特定的元素，我们需要执行一些业务。
方法1：
```
if (values.stream().anyMatch(s -> s.equals("two"))) {
    throw new RuntimeException("two was found");
}
改良版：
if (values.stream().anyMatch("two"::equals)) {
    throw new RuntimeException("two was found");
}
```
方法二：
```
values.stream()
            .filter("two"::equals)
            .findAny()
            .ifPresent(s -> {
                throw new RuntimeException("found");
            });
```

---

##### 对象集合根据key归集，转化为Map<String,List<Object>>

源数据：List<Person> ，根据Person对象的age属性，归集成Map，目标数据结构：Map<Integer,List<Person>>
```
        Person p1 = new Person("alan", 19, new BigDecimal("100"));
        Person p2 = new Person("bob", 21, new BigDecimal("100"));
        Person p3 = new Person("candy", 21, new BigDecimal("100"));
        Person p4 = new Person("django", 25, new BigDecimal("300"));
        Person p5 = new Person("ella", 22, new BigDecimal("200"));
        List<Person> list = Arrays.asList(p1,p2,p3, p4,p5);
        Map<Integer, List<Person>> map = list.stream()
                .collect(Collectors.groupingBy(Person::getAge,
                        Collectors.mapping(a -> a, Collectors.toList())));
  
// 源数据：
[
    {
        "age": 19,
        "money": 100,
        "name": "alan"
    },
    {
        "age": 21,
        "money": 100,
        "name": "bob"
    },
    {
        "age": 21,
        "money": 100,
        "name": "candy"
    },
    {
        "age": 25,
        "money": 300,
        "name": "django"
    },
    {
        "age": 22,
        "money": 200,
        "name": "ella"
    }
]

// 目标数据：
{
  19: [
    {
      "age": 19,
      "money": 100,
      "name": "alan"
    }
  ],
  21: [
    {
      "age": 21,
      "money": 100,
      "name": "bob"
    },
    {
      "age": 21,
      "money": 100,
      "name": "candy"
    }
  ],
  22: [
    {
      "age": 22,
      "money": 200,
      "name": "ella"
    }
  ],
  25: [
    {
      "age": 25,
      "money": 300,
      "name": "django"
    }
  ]
}
```

---
### map根据key过滤
```
map.entrySet()
            .stream()
            .filter(entry -> predicate.test(entry.getValue()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
}