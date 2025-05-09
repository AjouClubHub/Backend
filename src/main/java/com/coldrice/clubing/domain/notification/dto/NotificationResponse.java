package com.coldrice.clubing.domain.notification.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.notification.entity.Notification;

public record NotificationResponse(
	Long id,
	String content,
	boolean isRead,
	String type,
	LocalDateTime createdAt
) {
	public static NotificationResponse from(Notification n) {
		return new NotificationResponse(
			n.getId(),
			n.getContent(),
			n.isRead(),
			n.getType().name(),
			n.getCreatedAt()
		);
	}
}
