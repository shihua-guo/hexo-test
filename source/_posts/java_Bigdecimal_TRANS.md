---
title: 【java】BigDecimal类型转化心得
categories: 后端
tags: 后端
date: 2019.06.04 14:52:57
---
在从数据库查询number类型的数据时，在java对应的就是BigDecimal类型。因为，在转化成字符串类型的时候，精度会发生改变。这里针对BigDecimal转化为字符串类型的一些情况做一下总结。
以下使用了6种方法对（数值0.0001）bigDecimal进行字符串转化。
1. BigDecimal的intValue方法。
2. BigDecimal的toPlainString。
3. BigDecimal的toEngineeringString。
4. BigDecimal的doubleValue。
5. BigDecimal的setScale 设置精度然后再toPlainString。
6. 小数格式化模板DecimalFormat
```
public class test{
  public static void main(String args[]) {
		BigDecimal bigdecimal = new BigDecimal(0.0001d);
		BigDecimal bigDecimal = (BigDecimal) bigdecimal;
		int intValue = bigdecimal.intValue();
		String toPlainString = bigDecimal.stripTrailingZeros().toPlainString();
		String engineeringString = bigdecimal.toEngineeringString();
		Double doubleValue = bigDecimal.doubleValue();
		String setScaleToPlainString = bigDecimal.setScale(bigdecimal.scale(), RoundingMode.HALF_UP).toPlainString();
		System.out.println("intValue:"+intValue);
		System.out.println("toPlainString:"+toPlainString);
		System.out.println("engineeringString:"+engineeringString);
		System.out.println("doubleValue:"+doubleValue);
		System.out.println("setScaleToPlainString:"+setScaleToPlainString);
		
		NumberFormat nf1 = new DecimalFormat("################################################.###########################################");
		String decimalFormat = nf1.format(doubleValue);
		System.out.println("decimalFormat:"+decimalFormat);
  }
}
```
对应输出结果：
```
intValue:0
toPlainString:0.000100000000000000004792173602385929598312941379845142364501953125
engineeringString:0.000100000000000000004792173602385929598312941379845142364501953125
doubleValue:1.0E-4
setScaleToPlainString:0.000100000000000000004792173602385929598312941379845142364501953125
decimalFormat:0.0001
```
#### 总结
根据上面这种情况。只有最后一种（decimalFormat）转化出来的字符串是符合要求的。但是前提是要定义好足够长度的整数位和小数位。