package com.coldrice.clubing.domain.membership.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클럽 탈퇴 요청")
public record ClubWithdrawRequest(
	@Schema(description = "탈퇴 사유", example = "개인 사정으로 탈퇴합니다.")
	String leavenReason
) {
}
