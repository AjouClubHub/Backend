package com.coldrice.clubing.domain.application.entity;

import java.util.Arrays;

public enum ApplicationStatus {
	PENDING, APPROVED, REJECTED;

	public static ApplicationStatus of(String role) {
		return Arrays.stream(ApplicationStatus.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
