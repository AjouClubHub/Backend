package com.coldrice.clubing.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
	@NotBlank String name,
	@NotBlank @Email
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@ajou\\.ac\\.kr$", message = "ajou.ac.kr 이메일만 허용됩니다.")
	String email,
	@NotBlank String password,
	String major,
	@NotBlank String memberRole,
	@NotBlank Long studentId
) {
}
