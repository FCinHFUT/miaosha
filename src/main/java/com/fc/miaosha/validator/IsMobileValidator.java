package com.fc.miaosha.validator;
import  javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.fc.miaosha.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import com.fc.miaosha.util.ValidatorUtil;

public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

	private boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required) { //查看值是否是必须的
			return ValidatorUtil.isMobile(value);
		}else {
			if(StringUtils.isEmpty(value)) { //required
				return true;
			}else {
				return ValidatorUtil.isMobile(value);
			}
		}
	}

}
