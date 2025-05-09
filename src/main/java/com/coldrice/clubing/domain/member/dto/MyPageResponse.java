package com.coldrice.clubing.domain.member.dto;

import java.util.List;

import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageResponse(
	@Schema(description = "회원 정보")
	MemberProfileResponse member,
	@Schema(description = "가입된 클럽 목록")
	List<MyClubResponse> joinedClubs,
	@Schema(description = "가입 신청 목록")
	List<ApplicationResponse> applications,
	@Schema(description = "읽지 않은 알림 목록")
	List<NotificationResponse> notifications
) {
}
