package com.fc.miaosha.redis;
/*
用户UserKey的key的过期时间为不会过期。
 */
public class UserKey extends BasePrefix{

	private UserKey(String prefix) {
		super(prefix);
	}
	public static UserKey getById = new UserKey("id");
	public static UserKey getByName = new UserKey("name");
}
