package com.coldrice.clubing.domain.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.dto.ClubApplicationDecisonRequest;
import com.coldrice.clubing.domain.application.dto.RejectionReasonResponse;
import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.RequiredMajor;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

	private final ClubRepository clubRepository;
	private final ApplicationRepository applicationRepository;
	private final MembershipRepository membershipRepository;

	@Transactional
	public ApplicationResponse apply(Long clubId, ApplicationRequest request, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		// 중복 신청 체크
		if (applicationRepository.existsByClubAndMember(club, member)) {
			throw new GlobalException(ExceptionCode.DUPLICATE_APPLICATION);
		}

		// 클럽의 전공 제한 조건 확인
		List<RequiredMajor> requiredMajors = club.getRequiredMajors();
		if (!requiredMajors.isEmpty()) {
			RequiredMajor applicantMajor = RequiredMajor.valueOf(member.getMajor());
			if (!requiredMajors.contains(applicantMajor)) {
				throw new GlobalException(ExceptionCode.MAJOR_REQUIREMENT_NOT_MET);
			}
		}

		Application application = Application.builder()
			.club(club)
			.member(member)
			.status(ApplicationStatus.PENDING)
			.birthDate(request.birthDate())
			.studentId(request.studentId())
			.major(member.getMajor())
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

	// 가입 승인/거절
	@Transactional
	public void decideApplication(Long clubId, Long applicationId, ClubApplicationDecisonRequest request,
		Member manager) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		// 본인이 관리하는 클럽인지 검증
		club.validateManager(manager);

		Application application = applicationRepository.findById(applicationId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_APPLICATION));

		// 신청이 해당 클럽에 대한 것인지 확인
		if (!application.getClub().equals(club)) {
			throw new GlobalException(ExceptionCode.INVALID_REQUEST);
		}

		// 이미 승인 처리 된것에 대한 예외처리
		if (application.getStatus() == ApplicationStatus.APPROVED) {
			throw new GlobalException(ExceptionCode.ALREADY_APPROVED);
		}

		ApplicationStatus requestedStatus = request.status();


		if (requestedStatus == ApplicationStatus.REJECTED) {
			String reason = request.rejectionReason();
			if (reason == null || reason.isBlank()) {
				throw new GlobalException(ExceptionCode.INVALID_REJECTED_REASON);
			}
			application.reject(reason); // 상태 + 사유 설정
		} else if (requestedStatus == ApplicationStatus.APPROVED) {
			application.approve(); // 상태만 변경

			// 중복된 멤버쉽 방지
			if (!membershipRepository.existsByMemberAndClub(application.getMember(), club)) {
				Membership membership = Membership.builder()
					.member(application.getMember())
					.club(club)
					.status(MembershipStatus.ACTIVE)
					.joinReason(application.getMotivation())
					.joinedAt(LocalDateTime.now())
					.build();
				membershipRepository.save(membership);
			}
		} else {
			throw new GlobalException(ExceptionCode.INVALID_REQUEST); // 에외 케이스 처리
		}
	}

	public List<ApplicationResponse> getMyApplications(Member member) {
		List<Application> applications = applicationRepository.findByMemberOrderByCreatedAtDesc(member); // 생성 일자로 내림차순
		return applications.stream()
			.map(ApplicationResponse::from)
			.toList();
	}

	public RejectionReasonResponse getRejectionReason(Long applicationId, Long memberId) {
		Application application = applicationRepository.findById(applicationId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_APPLICATION));

		if(!application.getMember().getId().equals(memberId)) {
			throw new GlobalException(ExceptionCode.UNAUTHORIZED_REQUEST);
		}

		if(application.getStatus() != ApplicationStatus.REJECTED) {
			throw new GlobalException(ExceptionCode.APPLICATION_NOT_REJECTED);
		}

		return RejectionReasonResponse.from(application);
	}

	@Transactional
	public void cancelApplication(Long clubId, Member member) {
		Application application = applicationRepository.findByClubIdAndMemberId(clubId, member.getId())
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_APPLICATION));

		if (application.getStatus() != ApplicationStatus.PENDING) {
			throw new GlobalException(ExceptionCode.CANNOT_CANCEL_NONE_PENDING_APPLICATION);
		}

		applicationRepository.delete(application);
	}
}
