package com.coldrice.clubing.domain.club.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클럽 정보 수정 요청")
public record ClubUpdateRequest(
	@Schema(description = "클럽 설명", example = "개발과 디자인 협업 동아리입니다.")
	String description,

	@Schema(description = "연락처", example = "010-9876-5432")
	String contactInfo,

	@Schema(description = "활동 장소", example = "원천관 105호")
	String location,

	@Schema(description = "키워드", example = "협업, 개발")
	String keyword
) {
}
