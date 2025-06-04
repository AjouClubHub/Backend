package com.coldrice.clubing.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public record SignupRequest(
	@Schema(description = "이름", example = "홍길동")
	@NotBlank String name,

	@Schema(description = "아주대학교 이메일", example = "test@ajou.ac.kr")
	@NotBlank @Email
	String email,

	@Schema(description = "비밀번호", example = "SecureP@ssw0rd!")
	@NotBlank String password,

	@Schema(description = "학과", example = "소프트웨어학과")
	String major,

	@Schema(description = "회원 역할", example = "MEMBER or MANAGER")
	@NotBlank String memberRole,

	@Schema(description = "학번", example = "202412345")
	@NotBlank String studentId
) {
}
