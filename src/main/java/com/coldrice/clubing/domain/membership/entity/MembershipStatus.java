package com.coldrice.clubing.domain.membership.entity;

import java.util.Arrays;

public enum MembershipStatus {
	ACTIVE, WITHDRAWN, EXPELLED; // 클럽 활동 중, 자발적 탈퇴, 강제 추방

	public static MembershipStatus of(String role) {
		return Arrays.stream(MembershipStatus.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
