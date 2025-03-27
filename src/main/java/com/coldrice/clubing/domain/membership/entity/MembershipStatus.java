package com.coldrice.clubing.domain.membership.entity;

import java.util.Arrays;

public enum MembershipStatus {
	PENDING, APPROVED, REJECTED, WITHDRAWN, EXPELLED;

	public static MembershipStatus of(String role) {
		return Arrays.stream(MembershipStatus.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
