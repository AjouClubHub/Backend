package com.coldrice.clubing.domain.club.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.club.entity.Club;

public interface ClubRepository extends JpaRepository<Club, Long> {
	boolean existsByName(String name);

	Optional<Club> findByName(String name);

	// 추가 고려 사항 : 해당 사용자가 관리하는 클럽 조회, 동아리/소학회 필터링 조회, 키워드 기반 검색
}
