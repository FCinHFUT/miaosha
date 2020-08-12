package com.fc.miaosha.vo;

import com.fc.miaosha.domain.OrderInfo;
/*
OrderDetailVo封装来专门给页面传值（json信息）
 */

public class OrderDetailVo {
	private GoodsVo goods;
	private OrderInfo order;
	public GoodsVo getGoods() {
		return goods;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	public OrderInfo getOrder() {
		return order;
	}
	public void setOrder(OrderInfo order) {
		this.order = order;
	}
}
