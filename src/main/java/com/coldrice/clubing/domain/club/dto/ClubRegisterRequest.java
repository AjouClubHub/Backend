package com.coldrice.clubing.domain.club.dto;

import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubType;

public record ClubRegisterRequest(
	String name,
	String description,
	ClubType type, // 동아리 또는 소학회
	ClubCategory category,
	String contactInfo,
	String location,
	String keyword,
	String joinRequirement
) {
}
