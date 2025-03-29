package com.coldrice.clubing.exception.customException;

import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public GlobalException(ExceptionCode exceptionCode) {
		super(exceptionCode.getMessage());
		this.exceptionCode = exceptionCode;
	}
}
