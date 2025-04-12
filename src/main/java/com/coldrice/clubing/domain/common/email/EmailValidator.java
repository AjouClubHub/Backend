package com.coldrice.clubing.domain.common.email;

import org.springframework.stereotype.Component;

import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

@Component
public class EmailValidator {

	private static final String ALLOWED_DOMAIN = "@ajou.ac.kr";

	public void validateAjouDomain(String email) {
		if (email == null || !email.endsWith(ALLOWED_DOMAIN)) {
			throw new GlobalException(ExceptionCode.INVALID_AJOU_EMAIL);
		}
	}
}
