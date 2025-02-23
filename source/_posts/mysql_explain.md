---
title: 【数据库】mysql explain 说明
categories: 数据库
tags: 数据库
date: 2021.12.10 15:43:06
---
# mysql explain 说明

> 总结学习一下mysql的explain 。innodb



| 列           | 说明                                                         |      |
| ------------ | ------------------------------------------------------------ | ---- |
| id           | 就是每个sql的编号，有以下原则：<br/>1.id越大，越优先执行。<br/>2. id相同就从上往下执行。<br/>3. id为null最后执行 |      |
| select_type  | 对应的查询类型（是简单查询还是复杂的联合）。<br/>simple：简单查询，不包含union和join。<br/>primay：最外层的select<br/>subquery：包含再select 中的查询<br/>derived：包含再from的子查询，就是一个临时的结果表。<br/>union：再union第二个跟着的select |      |
| table        | 对应的行访问那个表                                           |      |
| type         | 非常重要：代表对应的行关联或者访问的类型（就是访问是索引树还是常量等）。<br/>NULL：在优化阶段就已经得到了结果了，不需要再扫描索引了。比如：select min(id) from film; 【ID主键索引】<br/>const、system：就是可以直接将查询结果优化成常量返回。system就是const的特例，就是只有1条结果的常量<br/>eq_ref：直接关联或者匹配主键索引<br/>ref：不是主键索引，而是二级索引，需要回表(回溯主键索引拉取数据)<br/>range：查询一个索引的范围。<br/>index：需要全表扫描索引获取结果。就是不从B+树的根节点查找，而是扫描某个二级索引的叶子节点扫描（因为二级索引小，而聚簇索引大，如果扫描聚簇索引那么消耗的内存比较多），然后得到的主键再回表带出所有结果。<br/>ALL：全表扫描，直接扫描聚簇索引叶子节点的数据 |      |
| possible_key | 优化阶段mysql觉得可能走的key。具体的索引取决于执行的时候     |      |
| key          | 执行阶段真正走的索引。可以直接使用force_index强制走某个索引  |      |
| key_len      | 具体走了哪几个索引。比如索引：【name_age】，那么使用这个长度就是name字段的字节数+age的字节数。这个一般用再联合索引上，可以具体算出走了那几个索引，进行继续优化 |      |
| ref          | 这一列显示了在key列记录的索引中，表查找值所用到的列或常量，常见的有：const（常量），字段名（例：film.id） |      |
| rows         | 预估扫描的行数，不准确，仅供参考                             |      |
| Extra        | 显示额外的信息。<br/>Using Index：使用了覆盖索引。就是查询的值可以直接从对应的索引中全部带出【完美不需要优化】<br/>UsingWhere：使用where处理，并没有走覆盖索引？？？啥JB玩意，不懂【还可以，不需要优化】<br/>Using index condition：查询的列不能完全被索引覆盖。。Where前导列范围？？啥JB玩意，不懂【还可以，不需要优化】<br/>Using temporay：mysql 需要一个临时的地方处理。【一般，需要优化】<br/>Using filesort：参与排序的数据量非常大，无法再内存中处理【差劲！需要优化】<br/>Select tables optimized away：使用某些聚合函数（如min、max）。来访问索引再某个字段。<br/> |      |

