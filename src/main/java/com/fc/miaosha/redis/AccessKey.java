package com.fc.miaosha.redis;

public class AccessKey extends BasePrefix{
	//考虑页面缓存有效期比较短
	private AccessKey( int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}
	
	public static AccessKey withExpire(int expireSeconds) {
		return new AccessKey(expireSeconds, "access");
	}
	
}
