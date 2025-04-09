package com.coldrice.clubing.domain.application.dto;

import com.coldrice.clubing.domain.application.entity.ApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가입 신청 승인/거절 요청")
public record ClubApplicationDecisonRequest(
	@Schema(description = "처리 상태", example = "APPROVED or REJECTED. 처리 전엔 PENDING 상태")
	@NotNull ApplicationStatus status,

	@Schema(description = "거절 사유 (REJECTED일 경우 필수)", example = "모집 요건과 맞지 않음")
	String rejectionReason
) {
}
