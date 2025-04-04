package com.coldrice.clubing.domain.membership.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MembershipService {

	private final MembershipRepository membershipRepository;

	public List<MyClubResponse> getMyClubs(Member member) {
		// 가입 상태이면서 탈퇴하지 않은 (leftAt IS NULL) 클럽만 조회
		List<Membership> memberships = membershipRepository.findByMemberAndLeftAtIsNull(member);
		return memberships.stream()
			.map(MyClubResponse::from)
			.toList();

	}
}
