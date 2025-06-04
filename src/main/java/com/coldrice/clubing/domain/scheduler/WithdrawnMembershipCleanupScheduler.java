package com.coldrice.clubing.domain.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawnMembershipCleanupScheduler {

	private final MembershipRepository membershipRepository;

	@Scheduled(cron = "0 15 0 * * *")
	@Transactional
	public void deleteDeactivatedMembers() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(30); // 예: 탈퇴 후 30일 지난 멤버십 삭제
		List<Membership> toDelete = membershipRepository.findByStatusAndUpdatedAtBefore(MembershipStatus.WITHDRAWN,
			cutoff);

		membershipRepository.deleteAll(toDelete);
		log.info("클럽 탈퇴 후 30일 지난 멤버십 {}건 삭제 완료", toDelete.size());
	}

}
