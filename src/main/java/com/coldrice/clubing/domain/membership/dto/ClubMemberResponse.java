package com.coldrice.clubing.domain.membership.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.membership.entity.Membership;

public record ClubMemberResponse(
	Long memberId,
	String name,
	Long studentId,
	String major,
	LocalDateTime joinedAt
) {
	public static ClubMemberResponse from(Membership membership) {
		return new ClubMemberResponse(
			membership.getMember().getId(),
			membership.getMember().getName(),
			membership.getMember().getStudentId(),
			membership.getMember().getMajor(),
			membership.getJoinedAt()
		);
	}
}
