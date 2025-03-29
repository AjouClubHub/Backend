package com.coldrice.clubing.exception.customException;

import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.Getter;

@Getter
public class NotValidTokenException extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public NotValidTokenException(ExceptionCode exceptionCode) {
		this.exceptionCode = exceptionCode;
	}
}
