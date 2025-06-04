package com.coldrice.clubing.unit;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.coldrice.clubing.domain.announcement.dto.AnnouncementRequest;
import com.coldrice.clubing.domain.announcement.entity.AnnouncementCategory;
import com.coldrice.clubing.domain.announcement.repository.AnnouncementRepository;
import com.coldrice.clubing.domain.announcement.service.AnnouncementService;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;
import com.coldrice.clubing.domain.schedule.dto.ScheduleRequest;
import com.coldrice.clubing.domain.schedule.repository.ScheduleRepository;
import com.coldrice.clubing.domain.schedule.service.ScheduleService;

@SpringBootTest
@ActiveProfiles("test")
public class NotificationConcurrencyTest {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	private AnnouncementService announcementService;
	@Autowired
	private ClubRepository clubRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MembershipRepository membershipRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private AnnouncementRepository announcementRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	private Member manager;
	private Club club;

	@BeforeEach
	void setup() {
		// 클린업
		notificationRepository.deleteAll();
		announcementRepository.deleteAll();
		scheduleRepository.deleteAll();
		membershipRepository.deleteAll();
		applicationRepository.deleteAll();
		clubRepository.deleteAll();
		memberRepository.deleteAll();

		// 클럽 관리자 생성
		manager = memberRepository.save(Member.builder()
			.email("manager@test.com")
			.studentId("0001")
			.major("SOFTWARE")
			.name("관리자")
			.password("encoded")
			.memberRole(MemberRole.MANAGER)
			.build());

		club = clubRepository.save(Club.builder()
			.name("공지테스트클럽")
			.manager(manager)
			.requiredMajors(Collections.emptyList())
			.status(ClubStatus.APPROVED)
			.build());

		// 가입된 회원 수 50명
		for (int i = 0; i < 50; i++) {
			Member member = memberRepository.save(Member.builder()
				.email("user" + i + "@test.com")
				.studentId("2023" + i)
				.major("SOFTWARE")
				.name("사용자" + i)
				.password("pw")
				.memberRole(MemberRole.MEMBER)
				.build());

			membershipRepository.save(Membership.builder()
				.club(club)
				.member(member)
				.status(MembershipStatus.ACTIVE)
				.build());
		}
	}

	@Test
	void 공지등록_시_알림이_모든_회원에게_정상적으로_전송된다() {
		// given
		AnnouncementRequest request = new AnnouncementRequest(
			"정기 모임 공지",
			"5월 30일에 모임이 있습니다.",
			AnnouncementCategory.모임
		);

		// when
		announcementService.createAnnouncement(club.getId(), request, manager);

		// then
		long actualNotificationCount = notificationRepository.count();
		long expected = 50L;  // 가입된 회원 수

		assertThat(actualNotificationCount).isEqualTo(expected);
	}

	@Test
	void 일정등록_시_알림이_모든_회원에게_정상적으로_전송된다() {
		// given
		ScheduleRequest request = new ScheduleRequest(
			"OT 일정 공지",
			"신입생 OT가 체육관에서 열립니다.",
			LocalDateTime.of(2025, 5, 30, 14, 0),
			LocalDateTime.of(2025, 5, 30, 17, 0)
		);

		// when
		scheduleService.createSchedule(club.getId(), request, manager);

		// then
		long actualNotificationCount = notificationRepository.count();
		long expected = 50L;  // 가입된 회원 수

		assertThat(actualNotificationCount).isEqualTo(expected);
	}

}
