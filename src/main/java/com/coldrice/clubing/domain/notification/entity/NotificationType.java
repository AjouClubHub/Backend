package com.coldrice.clubing.domain.notification.entity;

import java.util.Arrays;

public enum NotificationType {
	JOIN_APPROVED,
	JOIN_REJECTED,
	NOTICE_CREATED,
	SCHEDULE_ADDED;

	public static NotificationType of(String role) {
		return Arrays.stream(NotificationType.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리"));
	}
}
