package com.coldrice.clubing.domain.member.entity;

import java.util.Arrays;

public enum MemberRole {
	ADMIN, MEMBER, MANAGER;

	public static MemberRole of(String role) {
		return Arrays.stream(MemberRole.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
