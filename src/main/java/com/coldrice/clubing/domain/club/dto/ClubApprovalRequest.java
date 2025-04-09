package com.coldrice.clubing.domain.club.dto;

import com.coldrice.clubing.domain.club.entity.ClubStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클럽 등록 승인/거절 요청")
public record ClubApprovalRequest(
	@Schema(description = "승인 상태", example = "APPROVED or REJECTED")
	ClubStatus status,

	@Schema(description = "거절 사유 (REJECTED일 경우 필수)", example = "내용 부족")
	String rejectionReason
) {
}
