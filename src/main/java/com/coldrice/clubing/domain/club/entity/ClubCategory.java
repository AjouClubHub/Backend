package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubCategory {
	과학기술분과, 레저스포츠분과, 사회활동분과, 연행예술분과, 종교분과, 창작전시분과, 체육분과, 학술언론분과, 준동아리;

	public static ClubCategory of(String role) {
		return Arrays.stream(ClubCategory.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리"));
	}
}
