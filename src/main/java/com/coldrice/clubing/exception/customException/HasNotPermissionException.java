package com.coldrice.clubing.exception.customException;


import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class HasNotPermissionException extends RuntimeException {
	public final ExceptionCode exceptionCode;

	public HasNotPermissionException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
