package com.coldrice.clubing.domain.announcement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.announcement.dto.AnnouncementRequest;
import com.coldrice.clubing.domain.announcement.dto.AnnouncementResponse;
import com.coldrice.clubing.domain.announcement.entity.Announcement;
import com.coldrice.clubing.domain.announcement.repository.AnnouncementRepository;
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
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

	private final AnnouncementRepository announcementRepository;
	private final ClubRepository clubRepository;
	private final MembershipRepository membershipRepository;
	private final NotificationRepository notificationRepository;
	private final SseService sseService;

	@Transactional
	public AnnouncementResponse createAnnouncement(Long clubId, @Valid AnnouncementRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		Announcement announcement = Announcement.builder()
			.title(request.title())
			.content(request.content())
			.category(request.category())
			.club(club)
			.createdBy(member)
			.build();

		announcementRepository.save(announcement);

		// 알림 생성
		List<Membership> members = membershipRepository.findByClubIdAndStatus(club.getId(), MembershipStatus.ACTIVE);

		List<Notification> notifications = members.stream()
			.map(m -> Notification.from(
				m.getMember(),
				club.getName() + "에 새로운 공지사항이 등록되었습니다.",
				NotificationType.NOTICE_CREATED
			)).toList();

		notificationRepository.saveAll(notifications);

		// SSE 실시간 전송 추가
		notifications.forEach(notification ->
			sseService.sendNotification(
				notification.getReceiver().getId(),
				NotificationResponse.from(notification)
			)
		);

		return AnnouncementResponse.from(announcement);
	}

	@Transactional
	public AnnouncementResponse updateAnnouncement(Long clubId, Long announcementId, AnnouncementRequest request,
		Member member) {
		Announcement announcement = announcementRepository.findById(announcementId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_ANNOUNCEMENT));

		if (!announcement.getClub().getId().equals(clubId)) {
			throw new GlobalException(ExceptionCode.INVALID_REQUEST); // 클럽과 공지 불일치
		}

		if (!announcement.getCreatedBy().equals(member)) {
			throw new GlobalException(ExceptionCode.UNAUTHORIZED_MANAGER);
		}

		announcement.update(request.title(), request.content());
		return AnnouncementResponse.from(announcement);
	}

	public List<AnnouncementResponse> getAnnouncementsByClubId(Long clubId) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		List<Announcement> announcements = announcementRepository.findByClubOrderByCreatedAtDesc(club);
		return announcements.stream()
			.map(AnnouncementResponse::from)
			.toList();
	}

	public AnnouncementResponse getAnnouncementById(Long clubId, Long announcementId) {
		Announcement announcement = announcementRepository.findByIdAndClubId(announcementId, clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_ANNOUNCEMENT));

		announcement.increaseView(); // 조회수 증가
		announcementRepository.save(announcement); // 조회수 반영 저장

		return AnnouncementResponse.from(announcement);
	}

}
