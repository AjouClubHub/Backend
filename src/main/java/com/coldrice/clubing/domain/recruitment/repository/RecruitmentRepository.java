package com.coldrice.clubing.domain.recruitment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.recruitment.entity.Recruitment;
import com.coldrice.clubing.domain.recruitment.entity.RecruitmentStatus;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
	boolean existsByClubId(Long clubId);

	Optional<Recruitment> findByClubId(Long clubId);

	List<Recruitment> findByStatusOrderByCreatedAtDesc(RecruitmentStatus status);

	List<Recruitment> findByStatus(RecruitmentStatus status);
}
