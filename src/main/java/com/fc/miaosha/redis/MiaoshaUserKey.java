package com.fc.miaosha.redis;
/*
秒杀用户的MiaoshaUserKey ，继承了super(expireSeconds,prefix)，可以设置有效期时间为2天。
 */
public class MiaoshaUserKey extends BasePrefix{

	public static final int TOKEN_EXPIRE = 3600*24 * 2;
	private MiaoshaUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");
	//对象缓存一般没有有效期，永久有效
	public static MiaoshaUserKey getById = new MiaoshaUserKey(0, "id");
}
