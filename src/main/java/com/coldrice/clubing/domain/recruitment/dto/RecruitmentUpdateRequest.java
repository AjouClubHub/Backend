package com.coldrice.clubing.domain.recruitment.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;

@Schema(description = "모집 공고 수정 요청")
public record RecruitmentUpdateRequest(
	@Schema(description = "제목", example = "2025 부원 모집 수정본")
	String title,

	@Schema(description = "모집 요건", example = "기초 Java 가능자 / 2학년 이상")
	String requirements,

	@Schema(description = "상시 모집 여부", example = "false")
	Boolean alwaysOpen,

	@Schema(description = "시작일", example = "2025-04-10")
	@FutureOrPresent
	LocalDate startDate,

	@Schema(description = "마감일", example = "2025-04-30")
	@FutureOrPresent
	LocalDate endDate
) {
}
