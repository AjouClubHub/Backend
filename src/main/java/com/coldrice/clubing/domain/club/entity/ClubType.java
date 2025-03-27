package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubType {
	과학기술, 레저스포츠, 사회활동, 연행예술, 종교,
	창작전시, 체육, 학술언론;

	public static ClubType of(String role) {
		return Arrays.stream(ClubType.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
