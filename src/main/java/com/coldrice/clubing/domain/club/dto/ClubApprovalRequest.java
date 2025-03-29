package com.coldrice.clubing.domain.club.dto;

import com.coldrice.clubing.domain.club.entity.ClubStatus;

public record ClubApprovalRequest(
	ClubStatus status, // APPROVED 또는 REJECTED
	String rejectionReason // 거절일 때만 입력
) {
}
