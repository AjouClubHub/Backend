package com.coldrice.clubing.domain.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.member.entity.Member;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
	boolean existsByClubAndMember(Club club, Member member);
}
