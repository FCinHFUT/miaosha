package com.fc.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;
/*
为什么做MD5？
如果不做任何处理：那么明文密码就会在网络上进行传输，假如说恶意用户取得这个数据包，那么就可以得到这个密码，所以不安全。

为什么做两次MD5？
用户端：PASS=MD5(明文+固定Salt)
服务端：PASS=MD5（用户输入+随机Salt）
（1）第一次 （在前端加密，客户端）：密码加密是（明文密码+固定盐值）生成md5用于传输，目的由于http是明文传输，
        当输入密码若直接发送服务端验证，此时被截取将直接获取到明文密码，获取用户信息。
目的： 加盐值是为了混淆密码，原则就是明文密码不能在网络上传输。

（2）第二次：服务端接收到已经计算过依次MD5的密码后，我们并不是直接存至数据库里面，而是生成一个随机的salt，
		跟用户输入的密码一起拼装，再做一次MD5，然后再把最终密码存在数据库里面。
目的： 防止数据库被入侵，被人通过彩虹表反查出密码。所以服务端接受到后，也不是直接写入到数据库，
	   而是生成一个随机盐(salt)，再进行一次MD5后存入数据库。
 */


public class MD5Util {
	
	public static String md5(String src) {
		return DigestUtils.md5Hex(src); //现成的MD5包
	}
	
	private static final String salt = "1a2b3c4d"; //客户端固定的salt，跟用户的密码做一个拼装
	
	public static String inputPassToFormPass(String inputPass) {
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		System.out.println(str);
		return md5(str);//char类型计算会自动转换为int类型，第一次MD5：PASS=MD5(明文+固定Salt)
	}

	//第二次MD5，随机salt，存到DB
	public static String formPassToDBPass(String formPass, String salt) {//随机的salt
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}

	//数据库md5,使用数据库随机salt
	public static String inputPassToDbPass(String inputPass, String saltDB) {
		String formPass = inputPassToFormPass(inputPass);
		String dbPass = formPassToDBPass(formPass, saltDB);
		return dbPass;
	}
	
	public static void main(String[] args) {
		System.out.println(inputPassToFormPass("123456"));//d3b1294a61a07da9b49b6e22b2cbd7f9
//		System.out.println(formPassToDBPass(inputPassToFormPass("123456"), "1a2b3c4d"));
//		System.out.println(inputPassToDbPass("123456", "1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449
	}
	
}
