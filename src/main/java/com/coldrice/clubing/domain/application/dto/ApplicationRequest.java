package com.coldrice.clubing.domain.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationRequest(
	@NotBlank String birthDate,
	@NotBlank String studentId,
	@NotBlank String gender,
	@NotBlank String phoneNumber,
	@NotBlank String motivation
) {
}
