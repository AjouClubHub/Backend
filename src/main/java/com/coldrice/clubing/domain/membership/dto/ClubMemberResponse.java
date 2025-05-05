package com.coldrice.clubing.domain.membership.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.membership.entity.Membership;

public record ClubMemberResponse(
	Long memberId,
	String name,
	String studentId,
	String major,
	LocalDateTime joinedAt,
	MemberRole memberRole
) {
	public static ClubMemberResponse from(Membership membership) {
		return new ClubMemberResponse(
			membership.getMember().getId(),
			membership.getMember().getName(),
			membership.getMember().getStudentId(),
			membership.getMember().getMajor(),
			membership.getJoinedAt(),
			membership.getMember().getMemberRole()
		);
	}
}
