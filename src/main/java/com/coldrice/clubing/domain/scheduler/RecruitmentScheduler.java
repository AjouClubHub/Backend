package com.coldrice.clubing.domain.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.recruitment.entity.Recruitment;
import com.coldrice.clubing.domain.recruitment.entity.RecruitmentStatus;
import com.coldrice.clubing.domain.recruitment.repository.RecruitmentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitmentScheduler {

	private final RecruitmentRepository recruitmentRepository;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
	@Transactional
	public void autoCloseExpiredRecruitments() {
		LocalDate today = LocalDate.now();
		List<Recruitment> openRecruitments = recruitmentRepository.findByStatus(RecruitmentStatus.OPEN);

		int closedCount = 0;

		for (Recruitment r : openRecruitments) {
			if (r.getEndDate().isBefore(today)) {
				r.close();
				closedCount++;
			}
		}

		log.info("자동 모집 마갑 완료: {}건 closed 처리됨", closedCount);
	}
}
