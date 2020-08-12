package com.fc.miaosha.exception;

import com.fc.miaosha.result.CodeMsg;

/*全局异常处理
定义一个全局异常GlobalException，出现异常就可以直接抛这个异常即可。
GlobalException()继承Runtime类，重写构造函数，传入CodeMsg 。

全局异常处理场景：先检查异常类型，若是我们业务异常，返回即可。业务中发现异常直接抛出我们自定义的异常即可。
 */

public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}

	public CodeMsg getCm() {
		return cm;
	}

}
