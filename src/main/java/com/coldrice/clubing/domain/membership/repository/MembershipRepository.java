package com.coldrice.clubing.domain.membership.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
	boolean existsByMemberAndClub(Member member, Club club);

	List<Membership> findByMemberAndLeftAtIsNull(Member member);
}
