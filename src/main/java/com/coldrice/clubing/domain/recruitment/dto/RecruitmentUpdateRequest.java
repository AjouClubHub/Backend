package com.coldrice.clubing.domain.recruitment.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;

public record RecruitmentUpdateRequest(
	String title,
	String requirements,
	@FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
	LocalDate startDate,
	@FutureOrPresent(message = "마감일은 오늘 이후여야 합니다.")
	LocalDate endDate
) {
}
