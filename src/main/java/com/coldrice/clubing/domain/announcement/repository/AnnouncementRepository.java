package com.coldrice.clubing.domain.announcement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.announcement.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
}
