package com.fc.miaosha.rabbitmq;

import com.fc.miaosha.domain.MiaoshaUser;
//MiaoshaMessage 消息的封装类  MiaoshaMessage Bean
public class MiaoshaMessage {
	private MiaoshaUser user;
	private long goodsId;
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
}
