package com.coldrice.clubing.domain.membership.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubCategory;

public record ManagedClubResponse(
	Long id,
	String name,
	ClubCategory category,
	String location,
	String contactInfo,
	String keyword,
	String description,
	LocalDateTime createdAt,
	int memberCount,         // 현재 가입된 멤버 수
	int pendingApplications, // 대기 중인 가입 신청 수
	int announcementCount    // 공지사항 수
) {
	public static ManagedClubResponse from(Club club, int memberCount, int pendingApplications, int announcementCount) {
		return new ManagedClubResponse(
			club.getId(),
			club.getName(),
			club.getCategory(),
			club.getLocation(),
			club.getContactInfo(),
			club.getKeyword(),
			club.getDescription(),
			club.getCreatedAt(),
			memberCount,
			pendingApplications,
			announcementCount
		);
	}
}

