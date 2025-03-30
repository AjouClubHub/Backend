package com.coldrice.clubing.domain.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

	private final ClubRepository clubRepository;
	private final ApplicationRepository applicationRepository;

	@Transactional
	public ApplicationResponse apply(Long clubId, ApplicationRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		// 중복 신청 체크
		if (applicationRepository.existsByClubAndMember(club, member)) {
			throw new GlobalException(ExceptionCode.DUPLICATE_APPLICATION);
		}

		Application application = Application.builder()
			.club(club)
			.member(member)
			.status(ApplicationStatus.PENDING)
			.birthDate(request.birthDate())
			.studentId(request.studentId())
			.major(request.major())
			.gender(request.gender())
			.phoneNumber(request.phoneNumber())
			.motivation(request.motivation())
			.build();

		applicationRepository.save(application);
		return ApplicationResponse.from(application);
	}

	public List<ApplicationResponse> getAllApplications(Long clubId) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		List<Application> applications = applicationRepository.findByClubOrderByStatusAsc(club);
		return applications.stream()
			.map(ApplicationResponse::from).toList();
	}
}
