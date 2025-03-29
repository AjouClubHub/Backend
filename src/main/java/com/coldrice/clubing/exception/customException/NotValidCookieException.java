package com.coldrice.clubing.exception.customException;

import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class NotValidCookieException extends Exception {
	private final ExceptionCode exceptionCode;

	public NotValidCookieException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
