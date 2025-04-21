package com.coldrice.clubing.domain.membership.service;

import java.util.List;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.dto.ClubMemberResponse;
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

		Membership membership = membershipRepository.findByMemberIdAndClubId(member.getId(),clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_MEMBERSHIP));

		if (membership.getStatus() == MembershipStatus.WITHDRAWN || membership.getLeftAt() != null) {
			throw new GlobalException(ExceptionCode.ALREADY_WITHDRAWN);
		}

		membership.withdraw(reason);
	}

	public List<ClubMemberResponse> getClubMembers(Long clubId, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		return membershipRepository.findByClubIdAndStatus(clubId, MembershipStatus.ACTIVE)
			.stream()
			.map(ClubMemberResponse::from)
			.toList();
	}

	@Transactional
	public void expelClubMember(Long clubId, Long memberId, Member member, String reason) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		club.validateManager(member);

		Membership membership = membershipRepository.findByMemberIdAndClubId(memberId, clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_MEMBERSHIP));

		if (membership.getStatus() == MembershipStatus.EXPELLED || membership.getLeftAt() != null) {
			throw new GlobalException(ExceptionCode.AlREADY_EXPELLED);
		}

		membership.expel(reason);
	}

}
