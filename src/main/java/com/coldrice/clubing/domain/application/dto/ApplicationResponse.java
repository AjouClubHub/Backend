package com.coldrice.clubing.domain.application.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;

public record ApplicationResponse(
	Long applicationId,
	Long clubId,
	ClubType clubType,
	String clubName,
	String memberName,
	ApplicationStatus status,
	LocalDateTime appliedAt
) {
	public static ApplicationResponse from(Application application) {
		return new ApplicationResponse(
			application.getId(),
			application.getClub().getId(),
			application.getClub().getType(),
			application.getClub().getName(),
			application.getMember().getName(),
			application.getStatus(),
			application.getCreatedAt()
		);
	}
}
