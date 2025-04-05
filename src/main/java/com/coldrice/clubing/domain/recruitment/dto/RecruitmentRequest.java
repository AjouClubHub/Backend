package com.coldrice.clubing.domain.recruitment.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RecruitmentRequest(
	@NotBlank(message = "제목은 필수입니다.")
	String title,

	@NotBlank(message = "모집 요건은 필수입니다.")
	String requirements,

	@NotNull(message = "모집 시작일은 필수입니다.")
	@FutureOrPresent(message = "시작일은 오늘 이후여야 합니다.")
	LocalDate startDate,

	@NotNull(message = "모집 마감일은 필수입니다.")
	@FutureOrPresent(message = "마감일은 오늘 이후여야 합니다.")
	LocalDate endDate
) {
}
