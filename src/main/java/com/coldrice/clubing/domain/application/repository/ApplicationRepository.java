package com.coldrice.clubing.domain.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.application.entity.ApplicationStatus;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.member.entity.Member;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
	boolean existsByClubAndMember(Club club, Member member);

	List<Application> findByClubOrderByStatusAsc(Club club);

	List<Application> findByMemberOrderByCreatedAtDesc(Member member);

	int countByClubIdAndStatus(Long id, ApplicationStatus applicationStatus);

	Optional<Application> findByClubIdAndMemberId(Long clubId, Long memberId);

	List<Application> findByMember(Member member);

	List<Application> findByStatusAndCreatedAtBefore(ApplicationStatus applicationStatus, LocalDateTime cutoff);
}
