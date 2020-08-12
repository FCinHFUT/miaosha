package com.fc.miaosha.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.fc.miaosha.access.UserContext;
import com.fc.miaosha.domain.MiaoshaUser;
import com.fc.miaosha.service.MiaoshaUserService;
/*
创建一个UserArgumentResolver类并且实现接口HandlerMethodArgumentResolver，
然后重写里面的方法resolveArgument和supportsParameter方法，
既然要让MiaoshaUser的实例对象可以像SpringMVC中的controller方法中的HttpServletRequest的实例对象request一样可以直接使用，
那么解析前端传来的cookie里面的token或者请求参数里面的token的业务逻辑就在这里完成
 */

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

	@Autowired
	MiaoshaUserService userService;
	
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz==MiaoshaUser.class;
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return UserContext.getUser();
	}

}
