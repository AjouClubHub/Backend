package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubCategory {
	CLUB_A, CLUB_B;

	public static ClubCategory of(String role) {
		return Arrays.stream(ClubCategory.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
