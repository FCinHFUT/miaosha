package com.fc.miaosha.vo;

import java.util.Date;

import com.fc.miaosha.domain.Goods;
import com.fc.miaosha.domain.Goods;
/*
创建GoodsDao
注意：这里我们查数据库的时候，不只是查找的商品的信息，我们同时想把商品的秒杀信息也一起查出来，
但是这两个不同数据在两个表里面，我们就想办法封装一个GoodsVo，将两张表的数据封装到一起。

因为继承Goods，拥有Goods的所有字段，然后再自己定义MiaoshaGoods里面的字段，最终拼接成一个GoodsVo对象。
 */

public class GoodsVo extends Goods {
	private Double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
	public Integer getStockCount() {
		return stockCount;
	}
	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Double getMiaoshaPrice() {
		return miaoshaPrice;
	}
	public void setMiaoshaPrice(Double miaoshaPrice) {
		this.miaoshaPrice = miaoshaPrice;
	}
}
