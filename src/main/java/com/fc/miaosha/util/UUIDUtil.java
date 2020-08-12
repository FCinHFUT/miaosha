package com.fc.miaosha.util;

import java.util.UUID;
/* 分布式Session
我们的秒杀服务，实际的应用可能不止部署在一个服务器上，而是分布式的多台服务器，
这时候假如用户登录是在第一个服务器，第一个请求到了第一台服务器，但是第二个请求到了第二个服务器，
那么用户的session信息就丢失了。

解决：session同步，无论访问那一台服务器，session都可以取得到。
本系统：利用一台缓存服务器集中管理session，即利用缓存统一管理session。

注 ： 分布式Session的几种实现方式
1.基于数据库的Session共享
2.基于NFS共享文件系统
3.基于memcached 的session，如何保证 memcached 本身的高可用性？
4. 基于resin/tomcat web容器本身的session复制机制
5. 基于TT/Redis 或 jbosscache 进行 session 共享。
6. 基于cookie 进行session共享

本系统解决思路：
用户登录成功之后，给这个用户生成一个sessionId(用token来标识这个用户)，写到cookie中，传递给客户端。
然后客户端在随后的访问中，都在cookie中上传这个token，然后服务端拿到这个token之后，
就根据这个token来取得对应的session信息。token利用uuid生成。
 */

public class UUIDUtil {
	public static String uuid() {
        //去掉原生的"-"，因为原生会带有"-"
		return UUID.randomUUID().toString().replace("-", "");
	}
}
