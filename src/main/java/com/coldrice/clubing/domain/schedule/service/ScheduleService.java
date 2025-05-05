package com.coldrice.clubing.domain.schedule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.schedule.dto.ScheduleRequest;
import com.coldrice.clubing.domain.schedule.dto.ScheduleResponse;
import com.coldrice.clubing.domain.schedule.entity.Schedule;
import com.coldrice.clubing.domain.schedule.repository.ScheduleRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

	private final ClubRepository clubRepository;
	private final ScheduleRepository scheduleRepository;

	@Transactional
	public ScheduleResponse createSchedule(Long clubId, ScheduleRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		Schedule schedule = Schedule.builder()
			.club(club)
			.title(request.title())
			.content(request.content())
			.startTime(request.startTime())
			.endTime(request.endTime())
			.build();

		scheduleRepository.save(schedule);
		return ScheduleResponse.from(schedule);
	}
}
