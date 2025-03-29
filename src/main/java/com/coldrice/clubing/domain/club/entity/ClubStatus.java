package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubStatus {
	PENDING, APPROVED, REJECTED, DELETED;

	public static ClubStatus of(String role) {
		return Arrays.stream(ClubStatus.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
