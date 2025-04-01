package com.coldrice.clubing.domain.club.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.dto.ClubApprovalRequest;
import com.coldrice.clubing.domain.club.dto.ClubRegisterRequest;
import com.coldrice.clubing.domain.club.dto.ClubRegisterResponse;
import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubService {

	private final ClubRepository clubRepository;

	// 클롭 등록 신청
	@Transactional
	public ClubRegisterResponse register(ClubRegisterRequest request, Member manager) {
		if (clubRepository.existsByName(request.name())) {
			throw new GlobalException(ExceptionCode.ALREADY_REGISTERED_CLUB);
		}

		Club club = Club.builder()
			.name(request.name())
			.description(request.description())
			.type(ClubType.of(String.valueOf(request.type())))
			.category(ClubCategory.of(String.valueOf(request.category())))
			.contactInfo(request.contactInfo())
			.location(request.location())
			.keyword(request.keyword())
			.requiredMajors(request.requiredMajors())
			.manager(manager)
			.build();

		Club savedClub = clubRepository.save(club);
		return new ClubRegisterResponse(savedClub.getId(), savedClub.getName(), "PENDING");
	}

	public List<ClubResponse> getAllClubs() {
		List<Club> clubs = clubRepository.findAllByOrderByStatusAsc();
		return clubs.stream().map(ClubResponse::from).toList();
	}

	public List<ClubResponse> getApprovedClubs() {
		List<Club> clubs = clubRepository.findAllByStatus(ClubStatus.APPROVED);
		return clubs.stream().map(ClubResponse::from).toList();
	}

	@Transactional
	public void updateClubApproval(Long clubId, ClubApprovalRequest request) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		if(request.status() == ClubStatus.REJECTED && request.rejectionReason() == null) {
			throw new IllegalArgumentException("거절 사유는 필수입니다.");
		}

		club.updateStatus(request.status(), request.rejectionReason());
	}

	public ClubResponse getClubById(Long clubId) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		return ClubResponse.from(club);
	}
}
