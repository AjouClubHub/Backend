package com.coldrice.clubing.exception.enums;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

	// 권한/인증 관련
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	NO_PERMISSION(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHORIZED_MANAGER(HttpStatus.UNAUTHORIZED, "유효하지 않은 클럽 관리자입니다"),

	// 기타
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	// Member
	Member_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
	EMAIL_DUPLICATE(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
	INVALID_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST, "ajou.ac.kr 이메일만 허용됩니다."),

	// Club
	NOT_FOUND_CLUB(HttpStatus.NOT_FOUND, "해당 클럽을 찾을 수 없습니다."),
	ALREADY_REGISTERED_CLUB(HttpStatus.BAD_REQUEST, "이미 등록된 클럽입니다."),

	// Application
	DUPLICATE_APPLICATION(HttpStatus.BAD_REQUEST, "이미 해당 클럽에 가입 신청을 하셨습니다."),
	NOT_FOUND_APPLICATION(HttpStatus.NOT_FOUND, "해당 가입 신청을 찾을 수 없습니다."),
	INVALID_REJECTED_REASON(HttpStatus.BAD_REQUEST, "가입 신청 거절시 거절 사유는 필수입니다." ),
	MAJOR_REQUIREMENT_NOT_MET(HttpStatus.FORBIDDEN, "학과가 모집 요건에 부합하지 않습니다."),

	// Membership
	NOT_FOUND_MEMBERSHIP(HttpStatus.NOT_FOUND, "해당 클럽에 가입되어 있지 않습니다." ),
	ALREADY_WITHDRAWN(HttpStatus.FORBIDDEN, "이미 탈퇴 처리된 클럽입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
