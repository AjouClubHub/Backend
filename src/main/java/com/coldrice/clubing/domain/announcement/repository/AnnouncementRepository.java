package com.coldrice.clubing.domain.announcement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.announcement.entity.Announcement;
import com.coldrice.clubing.domain.club.entity.Club;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
	List<Announcement> findByClubOrderByCreatedAtDesc(Club club);
}
