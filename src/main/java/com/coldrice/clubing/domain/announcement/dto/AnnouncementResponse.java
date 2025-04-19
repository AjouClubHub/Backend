package com.coldrice.clubing.domain.announcement.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.announcement.entity.Announcement;
import com.coldrice.clubing.domain.announcement.entity.AnnouncementCategory;

public record AnnouncementResponse(
	Long id,
	String title,
	String content,
	LocalDateTime createdAt,
	String authorName,
	AnnouncementCategory category,
	int views
) {
	public static AnnouncementResponse from(Announcement announcement) {
		return new AnnouncementResponse(
			announcement.getId(),
			announcement.getTitle(),
			announcement.getContent(),
			announcement.getCreatedAt(),
			announcement.getCreatedBy().getName(),
			announcement.getCategory(),
			announcement.getViews()
		);
	}
}
