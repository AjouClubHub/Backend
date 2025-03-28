package com.coldrice.clubing.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
	@NotBlank String name,
	@NotBlank String email,
	@NotBlank String password,
	@NotBlank String department,
	@NotBlank String memberRole
) {
}
