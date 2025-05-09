package com.coldrice.clubing.domain.member.dto;

import java.util.List;

import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;

public record MyPageResponse(
	MemberProfileResponse member,
	List<MyClubResponse> joinedClubs,
	List<ApplicationResponse> applications,
	List<NotificationResponse> notifications
) {
}
