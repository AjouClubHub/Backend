package com.coldrice.clubing.domain.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.recruitment.entity.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
	boolean existsByClubId(Long clubId);
}
