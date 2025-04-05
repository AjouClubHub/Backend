package com.coldrice.clubing.domain.application.dto;

import com.coldrice.clubing.domain.application.entity.Application;

public record RejectionReasonResponse(
	String rejectionReason
) {
	public static RejectionReasonResponse from(Application application) {
		return new RejectionReasonResponse(application.getRejectionReason());
	}
}
