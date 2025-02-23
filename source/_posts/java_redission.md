---
title: Java 使用注解实现Redisson分布式锁
date: 2023-02-23 16:53:56
tags: 后端
---


## 简易的使用注解实现Redisson分布式锁



### 背景

目前的redis分布式锁（RedisLockWrapper），有几个缺点：

1. 使用复杂。需要引入业务之外的代码。后续更新比较麻烦
2. 不支持自动续命。
3. 服务宕机无法快速释放锁。



### Redisson分布式锁的有点

1. Redisson有自旋和超时放弃模式
2. Redisson有读写锁
3. Redisson通过自旋不断给锁续命，Redisson订阅了redis消息，业务执行完成阻塞的线程会立马感知到。
4. Redisson加锁了，但是服务挂掉，即使是强制shutdown，因为锁过期时间非常短，也无需手动删除。如果是计划内关闭，Redisson会在中断执行线程之后删除锁。
5. Redisson使用lua脚本保证原子性



### EasyLock使用方法

1. 直接在需要加分布式锁的方法添加注解：@EasyLock即可



#### EasyLock高级使用方法

1. 默认key：使用【类名+方法名】作为锁名----> @EasyLock
2. 全参数key：使用【类名+方法名+参数json化之后的MD5】作为锁名。
2. 单一参数key：使用【类名+方法名+入参属性值】作为锁名-----> @EasyLock(propertyAsKey = "ware.id")
3. try模式（尝试对应时间去抢占锁，如果抢占失败则返回），0则当场失败/或成功 -----> @EasyLock(waitMillisecond = 5000)
4. 读写锁模式（读读不互斥，读写互斥，写写互斥）@EasyLock(key = "SpiderV2Controller.ReadWriteLock",mode = RLockModeEnum.READ/RLockModeEnum.WRITE)
注意：

### EasyLock注意事项

1. 这个和其他的springaop一样，【在bean内调用bean的方法是无法aop的】，所以加的时候要注意
2. 【静态方法，内部类的方法无法被AOP】，所以也是不支持的



### 详细使用例子：
com.iceasy.bd.common.redis.example.RedissonExample

```JAVA
public class RedissonExample {

    /**
     * 最简易的方式：
     * 1. 分布式锁
     * 2. 使用类名+方法名作为锁名。
     *  如：【REDISSION_LOCK:com.iceasy.bd.common.redis.example.RedissonExample:simpleLock】
     * 3. 未抢到锁的线程会自旋等待，直到
     */
    @EasyLock
    private static void simpleLock(){

    }

    /**
     * 单一参数锁，用于降低锁粒度
     * @param accessToken
     * 1. 使用【类名+方法名+参数值】作为锁名。
     *  比如这里入参的accessToken对象的refreshToken，值为：abcdef，那么锁名如下：
     *  【REDISSION_LOCK:com.iceasy.bd.common.redis.example.RedissonExample:simpleLock:abcdef】
     *
     */
    @EasyLock(propertyAsKey = "accessToken.refreshToken")
    private static void propertyLock(AccessToken accessToken){

    }
    /**
     * 全参数锁
     * @param accessToken
     * 1. 使用【类名+方法名+参数json化之后的MD5】作为锁名。
     *  比如这里入参的accessToken对象，那么会把这个对象转换为json字符串，然后进行MD5，锁名如下：
     *  【REDISSION_LOCK:com.iceasy.bd.common.redis.example.RedissonExample:simpleLock:253f09ee1b2b45ca9430f5695f1372cc】
     *
     */
    @EasyLock(paramAsKey = true)
    private static void paramLock(AccessToken accessToken){

    }

    /**
     * 超时锁
     * 1. 这个锁是利用了redisson的tryLock
     * 2. 如果waitMillisecond为0，就可以快速失败。
     * 3. 如果是大于0的数值，那么就会尝试对应时间去抢占锁，如果抢占失败则返回
     */
    @EasyLock(waitMillisecond = 5000)
    private static void tryLock(){

    }

    /*----------------------------------------------读写锁---------------------------------------------*/
    // 读读不互斥、读写互斥、写写互斥
    // 场景：一个update接口，一个详情接口，如果需要保证强一致，就可以在update接口加上写锁，在列表接口加上读锁。
    /**
     * 读锁
     * 1. 需要和写锁配合，并且key必须一致
     */
    @EasyLock(key = "SpiderV2Controller.ReadWriteLock",mode = RLockModeEnum.READ)
    private static void readLock(){

    }
    /**
     * 写锁
     * 1. 需要和读锁配合，并且key必须一致
     */
    @EasyLock(key = "SpiderV2Controller.ReadWriteLock",mode = RLockModeEnum.WRITE)
    private static void writeLock(){

    }
    /*----------------------------------------------读写锁---------------------------------------------*/

}

```



### 总结

可能在嵌套事务会出现释放锁失败，因为spring 可能开启不同的线程执行。