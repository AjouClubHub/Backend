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
	UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED, "본인의 요청이 아닙니다."),
	INVALID_CODE(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 코드입니다." ),
	EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "인증되지 않은 이메일입니다." ),

	// 기타
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
	INVALID_AJOU_EMAIL(HttpStatus.BAD_REQUEST, "ajou.ac.kr 이메일만 허용됩니다."),

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
	APPLICATION_NOT_REJECTED(HttpStatus.BAD_REQUEST, "거절된 신청이 아닙니다."),
	ALREADY_APPROVED(HttpStatus.BAD_REQUEST, "이미 승인되었습니다." ),
	CANNOT_CANCEL_NONE_PENDING_APPLICATION(HttpStatus.BAD_REQUEST, "대기 상태가 아닌 신청은 취소할 수 없습니다."),

	// Membership
	NOT_FOUND_MEMBERSHIP(HttpStatus.NOT_FOUND, "해당 클럽에 가입되어 있지 않습니다." ),
	ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "이미 탈퇴 처리된 클럽입니다."),
	NOT_JOINED_CLUB(HttpStatus.BAD_REQUEST, "해당 클럽에 가입되지 않았습니다."),

	// Recruitment
	INVALID_RECRUITMENT_DATE(HttpStatus.BAD_REQUEST, "모집 시작일과 종료일을 다시 확인해주세요."),
	DUPLICATE_RECRUITMENT(HttpStatus.FORBIDDEN, "해당 클럽에 대한 모집 공고는 이미 존재합니다." ),
	NOT_FOUND_RECRUITMENT(HttpStatus.NOT_FOUND, "해당 모집 공고를 찾을 수 없습니다."),
	ALREADY_CLOSED_RECRUITMENT(HttpStatus.BAD_REQUEST, "이미 마감된 모집 공고입니다." ),

	// Announcement
	NOT_FOUND_ANNOUNCEMENT(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
	AlREADY_EXPELLED(HttpStatus.BAD_REQUEST, "이미 추방된(혹은 탈퇴한) 회원입니다." ),

	// sms
	PHONE_MISMATCH(HttpStatus.BAD_REQUEST, "전화번호가 일치하지 않습니다." ),

	// Schedule
	NOT_FOUND_SCHEDULE(HttpStatus.NOT_FOUND, "해당 일정을 찾을 수 업습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
