package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum ClubType {
	동아리, 소학회;

	public static ClubType of(String role) {
		return Arrays.stream(ClubType.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
