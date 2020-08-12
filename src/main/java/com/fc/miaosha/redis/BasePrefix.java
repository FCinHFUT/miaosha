package com.fc.miaosha.redis;

/*
BasePrefix 抽象类： 简单的实现一下KeyPrefix，定义成抽象类原因，防止不小心被创建，
我们不希望BasePrefix被实例化，因为抽象类不允许实例化。我们只希望它被继承。不同模块的前缀类都继承他。


注意：该类2种不同构造方法：用于继承。一个只带前缀名，一个带前缀名和过期时间。
当实现public BasePrefix(String prefix)的时候，我们将默认这个key不会失效，因为有一些场景，我们不希望key失效，
但是有些场景我们需要设置key的合适的有效期。
 */
public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;
	
	private String prefix;
	
	public BasePrefix(String prefix) {//0代表永不过期
		this(0, prefix);
	}
	
	public BasePrefix( int expireSeconds, String prefix) {
		this.expireSeconds = expireSeconds;
		this.prefix = prefix;
	}
	
	public int expireSeconds() {//默认0代表永不过期
		return expireSeconds;
	}

	public String getPrefix() {
		String className = getClass().getSimpleName();
		return className+":" + prefix;
	}

}
