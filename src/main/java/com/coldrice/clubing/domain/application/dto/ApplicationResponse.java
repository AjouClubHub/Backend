package com.coldrice.clubing.domain.application.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;

public record ApplicationResponse(
	Long applicationId,
	String clubName,
	String memberName,
	ApplicationStatus status,
	LocalDateTime appliedAt
) {
	public static ApplicationResponse from(Application application) {
		return new ApplicationResponse(
			application.getId(),
			application.getClub().getName(),
			application.getMember().getName(),
			application.getStatus(),
			application.getCreatedAt()
		);
	}
}
