package com.coldrice.clubing.domain.membership.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.membership.entity.Membership;

public record MyClubResponse(
	Long membershipId,
	Long clubId,
	String ClubName,
	LocalDateTime joinedAt
) {
	public static MyClubResponse from(Membership membership) {
		return new MyClubResponse(
			membership.getId(),
			membership.getClub().getId(),
			membership.getClub().getName(),
			membership.getJoinedAt()
		);
	}
}
