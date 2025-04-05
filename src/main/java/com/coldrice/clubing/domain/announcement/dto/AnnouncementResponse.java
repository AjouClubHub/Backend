package com.coldrice.clubing.domain.announcement.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.announcement.entity.Announcement;

public record AnnouncementResponse(
	Long id,
	String title,
	String content,
	LocalDateTime createdAt
) {
	public static AnnouncementResponse from(Announcement announcement) {
		return new AnnouncementResponse(
			announcement.getId(),
			announcement.getTitle(),
			announcement.getContent(),
			announcement.getCreatedAt()
		);
	}
}
