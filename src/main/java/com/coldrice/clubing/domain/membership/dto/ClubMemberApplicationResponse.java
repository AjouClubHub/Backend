package com.coldrice.clubing.domain.membership.dto;

import com.coldrice.clubing.domain.application.entity.Application;

public record ClubMemberApplicationResponse(
	String birthDate,
	String studentId,
	String gender,
	String phoneNumber,
	String motivation
) {
	public static ClubMemberApplicationResponse from(Application application) {
		return new ClubMemberApplicationResponse(
			application.getBirthDate(),
			application.getStudentId(),
			application.getGender(),
			application.getPhoneNumber(),
			application.getMotivation()
		);
	}
}
