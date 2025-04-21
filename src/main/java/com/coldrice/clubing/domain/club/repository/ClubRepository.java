package com.coldrice.clubing.domain.club.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.member.entity.Member;

public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {
	boolean existsByName(String name);

	Optional<Club> findByName(String name);

	List<Club> findAllByOrderByStatusAsc();

	List<Club> findAllByStatus(ClubStatus clubStatus);

	List<Club> findAllByManagerId(Long id);

	Optional<Club> findByIdAndManager(Long id, Member manager);
}
