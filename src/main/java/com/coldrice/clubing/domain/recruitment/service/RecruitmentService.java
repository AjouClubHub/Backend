package com.coldrice.clubing.domain.recruitment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentRequest;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentResponse;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentUpdateRequest;
import com.coldrice.clubing.domain.recruitment.entity.Recruitment;
import com.coldrice.clubing.domain.recruitment.entity.RecruitmentStatus;
import com.coldrice.clubing.domain.recruitment.repository.RecruitmentRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

	private final ClubRepository clubRepository;
	private final RecruitmentRepository recruitmentRepository;

	@Transactional
	public RecruitmentResponse registerRecruitment(Long clubId, @Valid RecruitmentRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		// 이미 모집 공고가 존재
		if (recruitmentRepository.existsByClubId(clubId)) {
			throw new GlobalException(ExceptionCode.DUPLICATE_RECRUITMENT);
		}

		Recruitment recruitment = Recruitment.builder()
			.club(club)
			.title(request.title())
			.requirements(request.requirements())
			.startDate(request.startDate())
			.endDate(request.endDate())
			.status(RecruitmentStatus.OPEN)
			.build();

		recruitmentRepository.save(recruitment);

		return RecruitmentResponse.from(recruitment);
	}

	public RecruitmentResponse getRecruitment(Long clubId) {
		Recruitment recruitment = recruitmentRepository.findByClubId(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_RECRUITMENT));

		return RecruitmentResponse.from(recruitment);
	}

	public List<RecruitmentResponse> getAllRecruitments() {
		List<Recruitment> recruitments = recruitmentRepository.findAll();
		return recruitments.stream()
			.map(RecruitmentResponse::from)
			.toList();
	}

	public RecruitmentResponse updateRecruitment(Long clubId, @Valid RecruitmentUpdateRequest request, Member member) {
		Recruitment recruitment = recruitmentRepository.findByClubId(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_RECRUITMENT));

		recruitment.getClub().validateManager(member);

		recruitment.update(
			request.title(),
			request.requirements(),
			request.startDate(),
			request.endDate()
		);

		return RecruitmentResponse.from(recruitment);
	}

	public List<RecruitmentResponse> getRecruitmentsByStatus(RecruitmentStatus status) {
		List<Recruitment> recruitments = recruitmentRepository.findByStatusOrderByCreatedAtDesc(status);
		return recruitments.stream()
			.map(RecruitmentResponse::from)
			.toList();
	}
}
