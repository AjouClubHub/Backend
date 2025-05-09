package com.coldrice.clubing.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.member.dto.MemberProfileResponse;
import com.coldrice.clubing.domain.member.dto.MyPageResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MembershipRepository membershipRepository;
	private final ApplicationRepository applicationRepository;
	private final NotificationRepository notificationRepository;

	public MyPageResponse getMyPage(Member member) {
		// 회원 정보
		MemberProfileResponse profile = MemberProfileResponse.from(member);

		// 승인된 클럽 목록
		List<MyClubResponse> clubs = membershipRepository.findByMemberAndStatus(member, MembershipStatus.ACTIVE)
			.stream()
			.map(MyClubResponse::from)
			.toList();

		// 가입 신청 현황
		List<ApplicationResponse> applications = applicationRepository.findByMember(member)
			.stream()
			.map(ApplicationResponse::from)
			.toList();

		// 안 읽은 알림
		List<NotificationResponse> notifications = notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(
				member)
			.stream()
			.map(NotificationResponse::from)
			.toList();

		return new MyPageResponse(profile, clubs, applications, notifications);
	}
}
