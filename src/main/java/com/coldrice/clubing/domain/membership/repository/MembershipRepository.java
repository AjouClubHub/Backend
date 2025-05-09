package com.coldrice.clubing.domain.membership.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.membership.entity.Membership;
import com.coldrice.clubing.domain.membership.entity.MembershipStatus;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
	boolean existsByMemberAndClub(Member member, Club club);

	List<Membership> findByMemberAndLeftAtIsNull(Member member);

	Optional<Membership> findByMemberIdAndClubId(Long id, Long clubId);

	List<Membership> findByClubIdAndStatus(Long clubId, MembershipStatus membershipStatus);

	int countByClubIdAndStatus(Long id, MembershipStatus membershipStatus);

	Optional<Membership> findByClubIdAndMemberId(Long clubId, Long memberId);

	List<Membership> findByMemberAndStatus(Member member, MembershipStatus membershipStatus);

	boolean existsByClubAndMember(Club club, Member member);

	List<Membership> findByStatus(MembershipStatus membershipStatus);

	List<Membership> findByStatusAndUpdatedAtBefore(MembershipStatus membershipStatus, LocalDateTime cutoff);
}
