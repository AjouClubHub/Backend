package com.coldrice.clubing.domain.recruitment.entity;

import java.util.Arrays;

public enum RecruitmentStatus {
	OPEN, CLOSED;

	public static RecruitmentStatus of(String role) {
		return Arrays.stream(RecruitmentStatus.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 스테이터스"));
	}
}
