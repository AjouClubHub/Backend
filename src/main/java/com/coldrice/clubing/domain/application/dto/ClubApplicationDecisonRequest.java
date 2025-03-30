package com.coldrice.clubing.domain.application.dto;

import com.coldrice.clubing.domain.application.entity.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

public record ClubApplicationDecisonRequest(
	@NotNull ApplicationStatus status, // APPROVED or REJECTED
	String rejectionReason // REJECTED 경우 필수
) {
}
