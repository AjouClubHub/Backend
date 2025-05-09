package com.coldrice.clubing.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;

	public List<NotificationResponse> getUnreadNotifications(Member member) {
		return notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(member)
			.stream()
			.map(NotificationResponse::from)
			.toList();
	}
}
