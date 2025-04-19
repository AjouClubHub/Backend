package com.coldrice.clubing.domain.announcement.entity;

import java.util.Arrays;

import com.coldrice.clubing.domain.club.entity.ClubCategory;

public enum AnnouncementCategory {
	모임, 스터디, 기타;

	public static ClubCategory of(String role) {
		return Arrays.stream(ClubCategory.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리"));
	}
}
