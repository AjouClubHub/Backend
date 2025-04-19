package com.coldrice.clubing.domain.common.sms.dto;

public record SmsVerificationRequest(
	String phoneNumber,
	String code
) {
}
