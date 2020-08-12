package com.fc.miaosha.vo;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fc.miaosha.validator.IsMobile;

/*
JSR303参数校验
系统在登录的时候做了一个参数校验，也就是说每一个方法的开头都要去做一个校验，那么有没有更简洁的方法呢？
那就是使用JSR 303 校验。 其对Java Bean 中的字段的值进行验证,使得验证逻辑从业务代码中脱离出来。
是一个运行时的数据验证框架，在验证之后验证的错误信息会被马上返回。

2.用法：
在需要验证的参数前面打上标签注解@Valid，那么此注解就会自动对该Bean 进行参数校验。具体校验规则在该Bean内部实现。
本项目是在登陆时候，利用到了参数校验。
 */

public class LoginVo {
	
	@NotNull
	@IsMobile
	private String mobile;
	
	@NotNull
	@Length(min=32)
	private String password;
	
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
	}
}
