package com.coldrice.clubing.domain.club.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.entity.RequiredMajor;

public record ClubResponse(
	Long id,
	String name,
	String description,
	ClubType type,
	ClubCategory category,
	String contactInfo,
	String location,
	String keyword,
	java.util.List<RequiredMajor> joinRequirement,
	ClubStatus status,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	String imaUrl,
	String snsUrl
) {
	// 팩토리 메서드
	public static ClubResponse from(Club club) {
		return new ClubResponse(
			club.getId(),
			club.getName(),
			club.getDescription(),
			club.getType(),
			club.getCategory(),
			club.getContactInfo(),
			club.getLocation(),
			club.getKeyword(),
			club.getRequiredMajors(),
			club.getStatus(),
			club.getCreatedAt(),
			club.getUpdatedAt(),
			club.getImageUrl(),
			club.getSnsUrl()
		);
	}
}
