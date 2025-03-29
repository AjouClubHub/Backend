package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubCategory {
	운동, 문화예술공연, IT, 봉사사회활동, 학술교양, 창업취업;

	public static ClubCategory of(String role) {
		return Arrays.stream(ClubCategory.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
