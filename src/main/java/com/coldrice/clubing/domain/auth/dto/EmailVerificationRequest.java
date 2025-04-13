package com.coldrice.clubing.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증코드 검증 요청")
public record EmailVerificationRequest(
	@NotBlank String email,
	@NotBlank String code
) {
}
