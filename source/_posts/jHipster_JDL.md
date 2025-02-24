---
title: 【后端】jHipster JDL 教程
categories: 后端
tags: 后端
date: 2017.02.15 18:18:09
---
> 写于2017-02-15

[JDL ](https://jhipster.github.io/jdl/)是[jHipster](https://jhipster.github.io/)专用的生成实体的语言。(大部分翻译自[JDL ](https://jhipster.github.io/jdl/))

在jHipster的项目，生成实体需要的文件：

- *a database table*(表中对应的表)

- *a Liquibase change set*(Liquibase 的changelog，在src\main\resources\config\liquibase\changelog目录下的文件)

- *a JPA entity class*(JPA实体类 src\main\java\com\ *xxx*\ *xxx*\domain目录下，应该是，错了请评论）

- *a Spring Data JpaRepository interface* (*src\main\java\com\binana\amas\repository* 目录下）

- a Spring MVC RestController class;

- an AngularJS router, controller and service; -src\main\webapp\app\entities\ 目录下

- a HTML page.-HTML文件

#### 导入.jh文件命令：
```
yo jhipster:import-jdl my_file1.jh my_file2.jh

jhipster-uml my_file1.jh my_file2.jh
```

*导入多个文件用空格隔开，需要把对应文件放在项目的根目录，不然会报错，说在根目录找不到对应jh文件。*

##### JDL实体示例：
```
entity <entity name> {
  <field name> <type> [<validation>*]
}
```

-   <entity name> 实体名称
-   <field name> 字段名，建议使用驼峰命名，使用下划线后面会出现问题
-   <type> 字段类型（需要jHipster支持的类型）
-   <validation> 字段的验证（是否为空，长度限制，正则等）

##### 例子：
```
entity A

entity B

entity C {}

entity D {

  name String required,

  address String required maxlength(100),

  age Integer required min(18)

}
```
 *说明：A ，B，C实体不包含任何字段，对于空实体这两种定义方式都可以。*

D实体包含：

名称为name，类型为string，不为空的字段。

名称为address，类型为string，不为空的字段且字符串最大长度为100的字段。

名称为age，类型为integer，不为空的字段且最小值为18的字段。

*如果需要验证，那么就在验证括号内加内容。*

|验证|说明|
|--|---|
|required|是否为空
|minlength(10):|字符串的最小长度|
|maxlength(100)|字符串的最大长度|
|pattern(*?[a-z])|正则|
|min(10)|数值的最小值|
|max(100)|数值的最大值|
|minbytes(512)|BLOB的最小字节|
|maxbytes(1024)|BLOB的最大字节|

 *JHipster会自动添加ID字段。*

下面是JDL支持的各种数据库对应的数据类型，JDL网站上面有给出。

|SQL |MongoDB |Cassandra |Validations|
|---|---|---|---|
| String | String | String | *required, minlength, maxlength, pattern* |
| Integer | Integer | Integer | *required, min, max* |
| Long | Long | Long | *required, min, max* |
| BigDecimal | BigDecimal | BigDecimal | *required, min, max* |
| Float | Float | Float | *required, min, max* |
| Double | Double | Double | *required, min, max* |
| Enum | Enum |   | *required* |
| Boolean | Boolean | Boolean | required |
| LocalDate | LocalDate |   | *required* |
|   |   | Date | *required* |
| ZonedDateTime | ZonedDateTime |   | *required* |
|   |   | UUID | *required* |
| Blob | Blob |   | *required, minbytes, maxbytes* |
| AnyBlob | AnyBlob |   | *required, minbytes, maxbytes* |
| ImageBlob | ImageBlob |   | *required, minbytes, maxbytes* |
| TextBlob | TextBlob |   | *required, minbytes, maxbytes*  |

##### 关系示例：
```
relationship (OneToMany | ManyToOne | OneToOne | ManyToMany) {
  <from entity>[{<relationship name>}] to <to entity>[{<relationship name>}]
}
```

- `(OneToMany | ManyToOne| OneToOne | ManyToMany)` 关系类型

-  `<from entity>` 关系拥有者的实体名

-  `<to entity>` 目标关系的实体名

- `<relationship name>` 关联另一方的那个字段

###### 同时可以在一个关系类型里面添加多个关系

```
entity A
entity B
entity C
entity D
relationship OneToOne {
  A{b} to B{a},
  B{c} to C
}
relationship ManyToMany {
  A{d} to D{a},
  C{d} to D{c}
}
```

###### 默认会关联ID字段。如果需要关联其他字段，那么在实体后的括号内加，如下：

```
entity A {
  name String required
}
entity B
relationship OneToOne {
  A{b} to B{a(name)}
}
```

枚举类型定义如下：

```
enum Language {
    FRENCH, ENGLISH, SPANISH
  }
```

在另外一个实体中引用如下：

```
  entity Book {
    title String required,
    description String,
    language Language
  }
```

***BLOB(byte[]):***

可以存放图片或者其他二进制类型的文件。

*   AnyBlob 或者就叫Blob 存放二进制文件的字段;
*   ImageBlob 意味着存放图片的字段.
*   TextBlob 存放长文本的字段.

##### 各种关系的前台和数据库的体现如下：

例子:
```
relationship ManyToMany {
 Job{task(title)} to Task{job}
}
```
*意思是：用Task实体的title做关联外键。*

JOB前台表示如下,显示的是task的title.实体中查看详细可以直接连接到对应的Task：

![image](/img/java/42.webp)

JOB数据库表现如下，并没有task_title：

![image](/img/java/43.webp)

其他可选的操作：

```
entity A {
  name String required
}
entity B {}
entity C {}
dto A, B with mapstruct
paginate A, C with infinite-scroll
paginate B with pager
service A with serviceClass
service C with serviceImpl
```
目前我只知道分页的方式其他不懂。

##### 分页的表现
  ![image](/img/java/44.webp "点击查看原始大小图片")

- NO：对应如下，有多少数据就显示多少：
![image](/img/java/45.webp "点击查看原始大小图片")

- Simple pager：
![image](/img/java/46.webp "点击查看原始大小图片")

- Pagination links：
![image](/img/java/47.webp "点击查看原始大小图片")

- infinite-scroll:
类似淘宝商品那样，触发到底部就加载接下来的数据。
![image](/img/java/48.webp)

