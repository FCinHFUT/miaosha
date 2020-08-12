package com.fc.miaosha.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fc.miaosha.access.AccessInterceptor;
/*
新建一个WebConfig类继承自WebMvcConfigurerAdapter，并且重写方法addArgumentResolvers，并且注入之前写好的UserArgumentResolver，
因为UserArgumentResolver 使用@Service标注，已经放到容器里面了，所以这里可以直接注入
 */


@Configuration
public class WebConfig  extends WebMvcConfigurerAdapter{
	
	@Autowired
	UserArgumentResolver userArgumentResolver;
	
	@Autowired
	AccessInterceptor accessInterceptor;

    /**
     * 设置一个MiaoshaUser参数给，toList、toDetail等controller方法使用使用
     */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessInterceptor);
	}
	
}
