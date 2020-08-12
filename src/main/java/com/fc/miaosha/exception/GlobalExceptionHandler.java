package com.fc.miaosha.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fc.miaosha.result.CodeMsg;
import com.fc.miaosha.result.Result;

/*
全局异常处理
当定义了JSR303校验器后，校验不通过都会产生一个BindException( org.springframework.validation.BindException)
和一大串错误信息（其中就包括校验的处理信息）。若要对异常处理，我们可以定义一个全局异常处理的拦截器。

这么做的好处：可以实现对项目中所有产生的异常进行拦截，在同一个类中实现统一处理。避免异常漏处理的情况。
 */


/*
@ControllerAdvice是一个@Component，用于定义@ExceptionHandler，@InitBinder和@ModelAttribute方法，
适用于所有使用@RequestMapping方法，会对所有@RequestMapping方法进行检查，拦截。并进行异常处理。
 */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	//拦截什么异常
	@ExceptionHandler(value=Exception.class) //拦截所有的异常
	public Result<String> exceptionHandler(HttpServletRequest request, Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException) {
			GlobalException ex = (GlobalException)e;
			return Result.error(ex.getCm());
		}else if(e instanceof BindException) {  //是绑定异常的情况
            //强转
			BindException ex = (BindException)e;
            //获取错误信息
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else { //不是绑定异常的情况
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
}
