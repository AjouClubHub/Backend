package com.coldrice.clubing.domain.schedule.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;
import com.coldrice.clubing.domain.notification.entity.Notification;
import com.coldrice.clubing.domain.notification.entity.NotificationType;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;
import com.coldrice.clubing.domain.notification.service.SseService;
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
	private final MembershipRepository membershipRepository;
	private final NotificationRepository notificationRepository;
	private final SseService sseService;

	@Transactional
	public ScheduleResponse createSchedule(Long clubId, ScheduleRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		Schedule schedule = Schedule.builder()
			.club(club)
			.title(request.title())
			.content(request.content())
			.startTime(request.startTime())
			.endTime(request.endTime())
			.build();

		scheduleRepository.save(schedule);

		// 알림 생성
		List<Membership> members = membershipRepository.findByClubIdAndStatus(club.getId(), MembershipStatus.ACTIVE);
		List<Notification> notifications = members.stream()
			.map(m -> Notification.from(
				m.getMember(),
				schedule.getClub().getName() + "에서 '" + schedule.getTitle() + "' 일정이 추가되었습니다.",
				NotificationType.SCHEDULE_ADDED
			)).toList();

		notificationRepository.saveAll(notifications);

		// SSE 실시간 전송 추가
		notifications.forEach(notification ->
			sseService.sendNotification(
				notification.getReceiver().getId(),
				NotificationResponse.from(notification)
			)
		);

		return ScheduleResponse.from(schedule);
	}

	public List<ScheduleResponse> getSchedulesByPeriod(Long clubId, LocalDateTime start, LocalDateTime end) {
		List<Schedule> schedule = scheduleRepository.findByClubIdAndStartTimeBetween(clubId, start, end);
		return schedule.stream()
			.map(ScheduleResponse::from)
			.toList();
	}

	public ScheduleResponse getSchedule(Long clubId, Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_SCHEDULE));

		if (!schedule.getClub().getId().equals(clubId)) {
			throw new GlobalException(ExceptionCode.INVALID_REQUEST); // 잘못된 클럽 접근 방지
		}

		return ScheduleResponse.from(schedule);
	}

	@Transactional
	public ScheduleResponse updateSchedule(Long clubId, Long scheduleId, ScheduleRequest request, Member member) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_SCHEDULE));

		schedule.getClub().validateManager(member);

		schedule.update(request.title(), request.content(), request.startTime(), request.endTime());

		return ScheduleResponse.from(schedule);
	}

	public void deleteSchedule(Long clubId, Long scheduleId, Member member) {
		Schedule schedule = scheduleRepository.findById(scheduleId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_SCHEDULE));

		schedule.getClub().validateManager(member);

		scheduleRepository.delete(schedule);
	}
}
