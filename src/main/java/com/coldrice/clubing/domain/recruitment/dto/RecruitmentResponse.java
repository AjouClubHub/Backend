package com.coldrice.clubing.domain.recruitment.dto;

import java.time.LocalDate;

import com.coldrice.clubing.domain.recruitment.entity.Recruitment;

public record RecruitmentResponse(
	Long id,
	String clubName,
	String title,
	String requirements,
	LocalDate startDate,
	LocalDate endDate
) {
	public static RecruitmentResponse from(Recruitment recruitment) {
		return new RecruitmentResponse(
			recruitment.getId(),
			recruitment.getClub().getName(),
			recruitment.getTitle(),
			recruitment.getRequirements(),
			recruitment.getStartDate(),
			recruitment.getEndDate()
		);
	}
}
