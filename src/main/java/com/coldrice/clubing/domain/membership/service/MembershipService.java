package com.coldrice.clubing.domain.membership.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.announcement.repository.AnnouncementRepository;
import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.dto.ClubMemberDetailWrapper;
import com.coldrice.clubing.domain.membership.dto.ClubMemberResponse;
import com.coldrice.clubing.domain.membership.dto.ManagedClubResponse;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {

	private final MembershipRepository membershipRepository;
	private final ClubRepository clubRepository;
	private final AnnouncementRepository announcementRepository;
	private final ApplicationRepository applicationRepository;

	public List<MyClubResponse> getMyClubs(Member member) {
		// 가입 상태이면서 탈퇴하지 않은 (leftAt IS NULL) 클럽만 조회
		List<Membership> memberships = membershipRepository.findByMemberAndLeftAtIsNull(member);
		return memberships.stream()
			.map(MyClubResponse::from)
			.toList();

	}

	public MyClubResponse getMyClubById(Long clubId, Member member) {
		Membership membership = membershipRepository.findByMemberIdAndClubId(member.getId(), clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_JOINED_CLUB));

		return MyClubResponse.from(membership);
	}

	@Transactional
	public void withdrawClub(Long clubId, Member member, String reason) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		Membership membership = membershipRepository.findByMemberIdAndClubId(member.getId(), clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_MEMBERSHIP));

		if (membership.getStatus() == MembershipStatus.WITHDRAWN || membership.getLeftAt() != null) {
			throw new GlobalException(ExceptionCode.ALREADY_WITHDRAWN);
		}

		membership.withdraw(reason);
	}

	public List<ClubMemberResponse> getClubMembers(Long clubId, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		// club.validateManager(member); 클럽 관리자 검증 삭제

		return membershipRepository.findByClubIdAndStatus(clubId, MembershipStatus.ACTIVE)
			.stream()
			.map(ClubMemberResponse::from)
			.toList();
	}

	public ClubMemberDetailWrapper getClubMember(Long clubId, Long memberId, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		Membership membership = membershipRepository
			.findByClubIdAndMemberId(clubId, memberId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_MEMBERSHIP));

		Application application = applicationRepository.findByClubIdAndMemberId(clubId, memberId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_APPLICATION));

		return ClubMemberDetailWrapper.of(membership, application);
	}

	// @Transactional
	// public void expelClubMember(Long clubId, Long memberId, Member member, String reason) {
	// 	Club club = clubRepository.findById(clubId)
	// 		.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));
	//
	// 	club.validateManager(member);
	//
	// 	Membership membership = membershipRepository.findByMemberIdAndClubId(memberId, clubId)
	// 		.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_MEMBERSHIP));
	//
	// 	if (membership.getStatus() == MembershipStatus.EXPELLED || membership.getLeftAt() != null) {
	// 		throw new GlobalException(ExceptionCode.AlREADY_EXPELLED);
	// 	}
	//
	// 	membership.expel(reason);
	// }

	public List<ManagedClubResponse> getMyManagedClubs(Member member) {
		List<Club> managedClubs = clubRepository.findAllByManagerId(member.getId());

		return managedClubs.stream()
			.map(club -> {
				int memberCount = membershipRepository.countByClubIdAndStatus(club.getId(), MembershipStatus.ACTIVE);
				int pendingCount = applicationRepository.countByClubIdAndStatus(club.getId(),
					ApplicationStatus.PENDING);
				int announcementCount = announcementRepository.countByClubId(club.getId());

				return ManagedClubResponse.from(club, memberCount, pendingCount, announcementCount);
			}).toList();
	}

	public ManagedClubResponse getMyManagedClubById(Long clubId, Member member) {
		Club club = clubRepository.findByIdAndManager(clubId, member)
			.orElseThrow(() -> new GlobalException(ExceptionCode.UNAUTHORIZED_MANAGER));

		int memberCount = membershipRepository.countByClubIdAndStatus(club.getId(), MembershipStatus.ACTIVE);
		int pendingCount = applicationRepository.countByClubIdAndStatus(club.getId(), ApplicationStatus.PENDING);
		int announcementCount = announcementRepository.countByClubId(club.getId());

		return ManagedClubResponse.from(club, memberCount, pendingCount, announcementCount);
	}

}
