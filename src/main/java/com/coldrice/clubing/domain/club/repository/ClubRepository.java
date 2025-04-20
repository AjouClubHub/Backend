package com.coldrice.clubing.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;

public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {
	boolean existsByName(String name);

	Optional<Club> findByName(String name);

	List<Club> findAllByOrderByStatusAsc();

	List<Club> findAllByStatus(ClubStatus clubStatus);

	// 추가 고려 사항 : 동아리/소학회 필터링 조회, 키워드 기반 검색
}
