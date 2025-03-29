package com.coldrice.clubing.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.customException.HasNotPermissionException;
import com.coldrice.clubing.exception.customException.NotValidCookieException;
import com.coldrice.clubing.exception.customException.NotValidTokenException;
import com.coldrice.clubing.exception.dto.NotValidRequestParameter;
import com.coldrice.clubing.exception.dto.ResponseExceptionCode;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(GlobalException.class)
	public ResponseEntity<Object> handleGlobalException(GlobalException e) {
		ExceptionCode exceptionCode = e.getExceptionCode();
		log.error("{}: {}", exceptionCode, exceptionCode.getMessage());
		return ResponseEntity.status(exceptionCode.getHttpStatus())
			.body(makeResponseExceptionCode(exceptionCode));
	}

	@ExceptionHandler(NotValidCookieException.class)
	public ResponseEntity<Object> handleNotValidCookieException(NotValidCookieException e) {
		ExceptionCode exceptionCode = e.getExceptionCode();
		log.error("{}: {}", exceptionCode, exceptionCode.getMessage());
		return ResponseEntity.status(exceptionCode.getHttpStatus())
			.body(makeResponseExceptionCode(exceptionCode));
	}

	@ExceptionHandler(NotValidTokenException.class)
	public ResponseEntity<Object> handleNotValidTokenException(NotValidTokenException e) {
		ExceptionCode exceptionCode = e.getExceptionCode();
		log.error("{}: {}", exceptionCode, exceptionCode.getMessage());
		return ResponseEntity.status(exceptionCode.getHttpStatus())
			.body(makeResponseExceptionCode(exceptionCode));
	}

	@ExceptionHandler(HasNotPermissionException.class)
	public ResponseEntity<Object> handleHasNotPermissionException(HasNotPermissionException e) {
		ExceptionCode exceptionCode = e.getExceptionCode();
		log.error("{}: {}", exceptionCode, exceptionCode.getMessage());
		return ResponseEntity.status(exceptionCode.getHttpStatus())
			.body(makeResponseExceptionCode(exceptionCode));
	}

	private ResponseExceptionCode makeResponseExceptionCode(ExceptionCode exceptionCode) {
		return ResponseExceptionCode.builder()
			.code(exceptionCode.name())
			.message(exceptionCode.getMessage())
			.build();
	}

	private NotValidRequestParameter makeNotValidRequestParameter(BindException e,
		ExceptionCode exceptionCode) {
		List<NotValidRequestParameter.NotValidParameter> notValidParameters = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(NotValidRequestParameter.NotValidParameter::of)
			.toList();

		return NotValidRequestParameter.builder()
			.code(exceptionCode.name())
			.message(exceptionCode.getMessage())
			.notValidParameters(notValidParameters)
			.build();
	}
}

