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
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

	private final AnnouncementRepository announcementRepository;
	private final ClubRepository clubRepository;

	@Transactional
	public AnnouncementResponse createAnnouncement(Long clubId, @Valid AnnouncementRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		Announcement announcement = Announcement.builder()
			.title(request.title())
			.content(request.content())
			.club(club)
			.createdBy(member)
			.build();

		announcementRepository.save(announcement);

		return AnnouncementResponse.from(announcement);
	}

	@Transactional
	public AnnouncementResponse updateAnnouncement(Long clubId, Long announcementId, AnnouncementRequest request, Member member) {
		Announcement announcement = announcementRepository.findById(announcementId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_ANNOUNCEMENT));

		if (!announcement.getClub().getId().equals(clubId)) {
			throw new GlobalException(ExceptionCode.INVALID_REQUEST); // 클럽과 공지 불일치
		}

		if(!announcement.getCreatedBy().equals(member)) {
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
