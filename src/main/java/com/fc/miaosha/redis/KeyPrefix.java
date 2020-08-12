package com.fc.miaosha.redis;
/*通用缓存Key封装 避免数据被修改。
模板模式
接口
抽象类
保证各个模块的key互补影响，加一个prefix前缀

（1）为什么要这个类？
当项目中的模块越来越多的时候，需要存的缓存也越来越多，比如商品Id,订单Id，用户id等,
此时若id出现重复，将给系统带来错误。
那么使用KeyPrefix来更好的操作和管理缓存中对应的key。给不同模块的key带有一个前缀。

（2）方法：利用一个前缀来规定不同模块的缓存的key,这样不同模块之间就不会重复。
（枚举不好，因为在该类中定义了缓存的时间）。

KeyPrefix区分各个模块，不同模块之间通过前缀就可以区分是不同的key,减少了查找的复杂性，
提高代码冗余度和优雅。更方便看，秒杀商品的过期时间等等也会用到et() set() 方法的时候。传入前缀，key,
值就可以完全区分开，不会取错，存错。
 */

public interface KeyPrefix {  //做缓存的前缀接口

	public int expireSeconds();
	
	public String getPrefix();
	
}
