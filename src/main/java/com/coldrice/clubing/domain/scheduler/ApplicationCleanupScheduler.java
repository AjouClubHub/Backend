package com.coldrice.clubing.domain.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationCleanupScheduler {

	private final ApplicationRepository applicationRepository;
	private final MembershipRepository membershipRepository;

	/**
	 * 💡 스케줄러 설명
	 * 매일 자정(00:00)에 실행됨.
	 * 클럽을 자발적으로 탈퇴한(WITHDRAWN) 멤버의 가입 신청 내역(Application)을 삭제합니다.
	 * 프론트 마이페이지에서 불필요한 신청 상태가 보이지 않도록 정리 목적.
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void deleteApplicationsOfWithdrawnMembers() {
		List<Membership> withdrawnMemberships = membershipRepository.findByStatus(MembershipStatus.WITHDRAWN);

		AtomicInteger deletedCount = new AtomicInteger();
		for (Membership membership : withdrawnMemberships) {
			Optional<Application> application = applicationRepository.findByClubIdAndMemberId(
				membership.getClub().getId(), membership.getMember().getId()
			);
			application.ifPresent(app -> {
				applicationRepository.delete(app);
				deletedCount.getAndIncrement();
			});
		}

		log.info("탈퇴한 멤버의 신청 내역 {}건 자동 삭제 완료", deletedCount.get());
	}

	/**
	 * 💡 7일 이상 지난 거절 신청 내역 정리
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void deleteOldRejectedApplications() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
		List<Application> oldRejected = applicationRepository.findByStatusAndCreatedAtBefore(
			ApplicationStatus.REJECTED, cutoff
		);

		applicationRepository.deleteAll(oldRejected);
		log.info("7일 이상 지난 거절된 신청 {}건 삭제 완료", oldRejected.size());
	}
}
