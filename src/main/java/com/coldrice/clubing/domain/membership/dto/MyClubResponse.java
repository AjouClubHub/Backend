package com.coldrice.clubing.domain.membership.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.membership.entity.Membership;

public record MyClubResponse(
	Long membershipId,
	Long clubId,
	ClubType clubType,
	String ClubName,
	String description,
	String location,
	String contactInfo,
	LocalDateTime joinedAt,
	String imgUrl
) {
	public static MyClubResponse from(Membership membership) {
		return new MyClubResponse(
			membership.getId(),
			membership.getClub().getId(),
			membership.getClub().getType(),
			membership.getClub().getName(),
			membership.getClub().getDescription(),
			membership.getClub().getLocation(),
			membership.getClub().getContactInfo(),
			membership.getJoinedAt(),
			membership.getClub().getImageUrl()
		);
	}
}
