package com.coldrice.clubing.domain.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.notification.service.NotificationService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NoticicationController {

	private final NotificationService notificationService;

	@Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
	@PatchMapping("/api/notifications/{notificationId}/read")
	public ResponseBodyDto<String> markAsRead(
		@PathVariable Long notificationId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		notificationService.markAsRead(notificationId, userDetails.getMember());
		return ResponseBodyDto.success("알림 읽음 처리 완료", null);
	}
}
