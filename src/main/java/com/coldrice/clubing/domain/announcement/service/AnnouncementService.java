package com.coldrice.clubing.domain.announcement.service;

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
}
