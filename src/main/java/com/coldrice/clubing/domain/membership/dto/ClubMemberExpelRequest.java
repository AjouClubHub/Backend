package com.coldrice.clubing.domain.membership.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "클럽 회원 추방 요청")
public record ClubMemberExpelRequest(
	@Schema(description = "추방 사유", example = "활동 불참이 지속되어 추방 처리합니다.")
	@NotBlank(message = "추방 사유는 필수입니다.")
	String reason
) {
}
