---
title: 【编程思想】functional programming
date: 2018-03-26 22:02:21
categories: 编程思想
tags: 函数式编程
---
# Functional Programming 
> 仅仅是一篇观后感

最近在我违反了 **no mutation** ，既在一个函数里面修改了传入的变量。导致我在另外一个地方使用该变量的时候，并不知道它已经发生了变化。然后，我突然想起了好几个月前看得一个演讲:[Anjana Vakil: Learning Functional Programming with JavaScript - JSUnconf 2016](https://www.youtube.com/watch?v=e-5obm1G_FY)。这里面就有讲什么是 **Functional Programming**。我发现，在JS中，遵循了下面几条原则，可以更好的复用、维护代码。

> 在平时写业务的过程中设计模式是谈不上了，但是Functional Programming恰处处可见。

### What is Functional Programming?
什么是Functional Programming
A programming paradigm.
一种编程的范式。就像**面向过程**、**面向对象**（狗.吃(屎)）。总的来说，**Function is King**
> 如果说**面向过程**就类似于（吃(狗,屎)）、**面向对象**类似于（狗.吃(屎)），那么Functional Programming可以初略概括为：狗.吃(屎) -> 屎，因为只有input和output才算是一个合格的function。

A code style.
一种代码的风格。如何去组织你的代码。

A mindset.
一种思维模式。该使用什么样的方式去解决你的问题？就像你不想去破解一个代码块完整性（内聚），那么你可以加入一个切面，去影响该代码块的执行结果。

A sexy, buzz-wordy trend.

### Why Functional Javascript?

Object-oriented in javascript gets tricky.
因为在JavaScript中，面向对象往往纠缠不清。就比如this.貌似真的很多时候，this的指向会变化多端。

Safer, easier to debug/maintain.

Established community.

### How Functional Programming in Javascript?

Do everything in function.
非常简单，就是一个input->output的过程。你只需要简单的把input交给一个function处理，然后它会给你需要的output。就像一种数据的流向。有以下的

###### 以下是非Functional的形式（A）：
```
var name = "Alan";
var greeting = "Hi,I'm ";
console.log(greeting+name);
=> "Hi,I'm Alan"
```
###### 以下是Functional的形式（B）：
```
function greet(name){
  return "Hi,I'm "+name;
}
greet("alan");
=> "Hi,I'm Alan"
```
例子A中，处理形式就是定义完greet，然后定义name，然后一起输出。而例子B，是将name交给一个greet函数处理，它会返回拼接一个greet然后返回给你。


Use pure function.
在Functional Programming中，我们会遇到一个问题：function a中，改变了input的内容，然后你在其他的function b中使用该input的时候，发现它已经被改变，然后也许function b中的执行结果会因为function a中改变了input而改变。这个就是文章开头提及的情况。这时候，你可能会绞尽脑汁，究竟在哪里改变了它。所以，纯净的function是不应该去改变input的。你应该在一个function里面拿了input，然后只读取input然后计算output，然后把output返回。
```
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
```
同样，以下也不是纯净的function
```
var name = "alan";
function greet(){
  console.log("Hi,I'm "+name);
}
```
并没有input,直接使用了全局的变量。而且并没有返回计算的结果，我们需要的是function帮我们计算并返回结果，打印并不是function需要做的事情。正确做法应该如下,function唯一需要做的就是使用input去计算得出我们需要的output，并将output返回：
```
var name = "alan";
function greet(name){
  return "Hi,I'm "+name;
}
```






