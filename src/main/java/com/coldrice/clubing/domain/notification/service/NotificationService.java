package com.coldrice.clubing.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;
import com.coldrice.clubing.domain.notification.entity.Notification;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

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

	public void markAsRead(Long notificationId, Member member) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_NOTIFICATION));

		// 수신자 검증
		if (!notification.getReceiver().getId().equals(member.getId())) {
			throw new GlobalException(ExceptionCode.UNAUTHORIZED_REQUEST);
		}

		notification.markAsRead();
	}
}
