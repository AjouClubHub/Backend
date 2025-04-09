package com.coldrice.clubing.domain.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "클럽 가입 신청 요청")
public record ApplicationRequest(
	@Schema(description = "생년월일", example = "2000-01-01")
	@NotBlank String birthDate,

	@Schema(description = "학번", example = "202412345")
	@NotBlank String studentId,

	@Schema(description = "성별", example = "남자 / 여자")
	@NotBlank String gender,

	@Schema(description = "전화번호", example = "010-1234-5678")
	@NotBlank String phoneNumber,

	@Schema(description = "지원 동기", example = "해당 동아리의 활동이 마음에 들어 지원합니다.")
	@NotBlank String motivation
) {
}
