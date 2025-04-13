package com.coldrice.clubing.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증코드 발송 요청")
public record EmailRequest(
	@NotBlank
	@Email
	String email
) {
}
