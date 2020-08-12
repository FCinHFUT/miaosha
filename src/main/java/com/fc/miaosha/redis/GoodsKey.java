package com.fc.miaosha.redis;
/*
作为页面缓存的缓存Key的前缀，缓存有效时间，一般设置为1分钟
一般这个页面缓存时间，也不会很长，防止数据的时效性很低。但是可以防止短时间大并发访问。
 */
public class GoodsKey extends BasePrefix{

	private GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
	public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
	public static GoodsKey getMiaoshaGoodsStock= new GoodsKey(0, "gs");
}
