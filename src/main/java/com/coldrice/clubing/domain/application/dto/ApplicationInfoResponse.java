package com.coldrice.clubing.domain.application.dto;

import com.coldrice.clubing.domain.application.entity.Application;

public record ApplicationInfoResponse(
	String birthDate,
	String studentId,
	String gender,
	String phoneNumber,
	String motivation
) {
	public static ApplicationInfoResponse from(Application application) {
		return new ApplicationInfoResponse(
			application.getBirthDate(),
			application.getStudentId(),
			application.getGender(),
			application.getPhoneNumber(),
			application.getMotivation()
		);
	}
}
