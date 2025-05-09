package com.coldrice.clubing.domain.recruitment.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "모집 공고 등록 요청")
public record RecruitmentRequest(
	@Schema(description = "모집 제목", example = "2025 상반기 부원 모집")
	@NotBlank(message = "제목은 필수입니다.")
	String title,

	@Schema(description = "모집 요건", example = "기초 Java 가능자 / 1학년 이상")
	@NotBlank(message = "모집 요건은 필수입니다.")
	String requirements,

	@Schema(description = "상시 모집 여부", example = "false")
	boolean alwaysOpen,

	@Schema(description = "시작일", example = "2025-04-10")
	@FutureOrPresent
	LocalDate startDate,

	@Schema(description = "마감일", example = "2025-04-30")
	@FutureOrPresent
	LocalDate endDate
) {
}
