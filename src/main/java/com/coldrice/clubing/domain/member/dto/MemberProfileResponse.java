package com.coldrice.clubing.domain.member.dto;

import com.coldrice.clubing.domain.member.entity.Member;

public record MemberProfileResponse(
	Long memberId,
	String name,
	String email,
	String major,
	String studentId
) {
	public static MemberProfileResponse from(Member member) {
		return new MemberProfileResponse(
			member.getId(),
			member.getName(),
			member.getEmail(),
			member.getMajor(),
			member.getStudentId()
		);
	}
}
