package com.coldrice.clubing.unit;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.application.service.ApplicationService;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ApplyConcurrencyTest {
	@Autowired
	private ApplicationService applicationService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ClubRepository clubRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private MembershipRepository membershipRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@BeforeEach
	void setup() {
		notificationRepository.deleteAll();
		memberRepository.deleteAll();
		applicationRepository.deleteAll();
		clubRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	void 동시에_가입신청을_보내면_모두_순서대로_성공한다() throws InterruptedException {
		// given
		Member manager = memberRepository.save(Member.builder()
			.email("manager@test.com")
			.studentId("9999")
			.major("SOFTWARE")
			.name("매니저")
			.password("encodedManagerPwd")
			.memberRole(MemberRole.MANAGER)
			.build());

		Club club = clubRepository.save(Club.builder()
			.name("동시성 테스트 클럽")
			.requiredMajors(Collections.emptyList())
			.manager(manager)
			.status(ClubStatus.APPROVED)
			.build());

		int threadCount = 50;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		CyclicBarrier barrier = new CyclicBarrier(threadCount); // 동시에 시작

		List<Future<String>> results = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			results.add(executor.submit(() -> {
				try {
					Member member = memberRepository.save(Member.builder()
						.email("user" + index + "@test.com")
						.studentId("2023" + index)
						.major("SOFTWARE")
						.name("사용자" + index)
						.password("encodedPwd123!")
						.build());

					ApplicationRequest request = new ApplicationRequest(
						"2000-01-01",
						"여자",
						"010-1111-11" + index,
						"동기",
						member.getStudentId()
					);

					barrier.await(); // 동시에 시작
					applicationService.apply(club.getId(), request, member);
					return "SUCCESS";
				} catch (Exception e) {
					return "FAIL: " + e.getMessage();
				} finally {
					latch.countDown();
				}
			}));
		}

		latch.await();

		long successCount = results.stream().filter(f -> {
			try {
				return f.get().equals("SUCCESS");
			} catch (Exception e) {
				return false;
			}
		}).count();

		results.forEach(f -> {
			try {
				System.out.println("결과: " + f.get());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		assertThat(successCount).isEqualTo(threadCount);
	}

}
