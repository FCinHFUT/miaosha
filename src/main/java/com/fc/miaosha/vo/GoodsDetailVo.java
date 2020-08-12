package com.fc.miaosha.vo;
/*
页面静态化+前后端分离
1. 常用技术AngularJS、Vue.js
2. 优点：利用浏览器的缓存

本文只是简单的实现页面静态化：
将页面直接缓存到用户的浏览器上面，好处：用户访问数据的时候，不用去请求服务器，直接在本地缓存中取得需要的页面缓存。

（1）未作页面静态化：请求某一个页面，访问缓存，查看缓存中是否有，缓存中有直接返回，缓存中没有的话，
				将数据渲染到html页面再存到缓存，再将整个html页面返回给客户端显示。
（2）做了页面静态化：第一次是去请求后台要渲染好的html页面，之后的请求都是直接访问用户本地浏览器的缓存的html页面 ，
				静态资源，然后前端通过Ajax来访问后端，只去获取页面需要显示的数据返回即可。
 */

import com.fc.miaosha.domain.MiaoshaUser;

/*
定义GoodsDetailVo封装来专门给页面传值。
 */
public class GoodsDetailVo {
	// 秒杀状态量初始值
	private int miaoshaStatus = 0;
	// 开始时间倒计时
	private int remainSeconds = 0;
	private GoodsVo goods ;
	private MiaoshaUser user;
	public int getMiaoshaStatus() {
		return miaoshaStatus;
	}
	public void setMiaoshaStatus(int miaoshaStatus) {
		this.miaoshaStatus = miaoshaStatus;
	}
	public int getRemainSeconds() {
		return remainSeconds;
	}
	public void setRemainSeconds(int remainSeconds) {
		this.remainSeconds = remainSeconds;
	}
	public GoodsVo getGoods() {
		return goods;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
}
