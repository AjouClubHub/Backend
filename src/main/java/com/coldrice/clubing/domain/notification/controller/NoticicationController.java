package com.coldrice.clubing.domain.notification.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.notification.dto.NotificationResponse;
import com.coldrice.clubing.domain.notification.service.NotificationService;
import com.coldrice.clubing.domain.notification.service.SseService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NoticicationController {

	private final SseService sseService;
	private final NotificationService notificationService;

	@Operation(
		summary = "알림 읽음 처리",
		description = """
			특정 알림을 읽음 상태로 변경합니다.
			읽음 처리 후 해당 알림은 마이페이지에서 조회되지 않습니다.
			
			사용 시점: 알림 클릭 후
			"""
	)
	@PatchMapping("/{notificationId}/read")
	public ResponseBodyDto<String> markAsRead(
		@PathVariable Long notificationId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		notificationService.markAsRead(notificationId, userDetails.getMember());
		return ResponseBodyDto.success("알림 읽음 처리 완료", null);
	}

	@Operation(
		summary = "전체 알림 조회",
		description = "현재 로그인한 사용자의 모든 알림을 조회합니다. (읽은 + 읽지 않은 알림 포함)"
	)
	@GetMapping
	public ResponseBodyDto<List<NotificationResponse>> getAllNotifications(
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<NotificationResponse> response = notificationService.getAllNotifications(userDetails.getMember());
		return ResponseBodyDto.success("알림 전체 조회 성공", response);
	}

	@Operation(
		summary = "SSE 실시간 알림 구독",
		description = """
			클라이언트가 실시간 알림을 수신하기 위해 SSE(Server-Sent Events) 연결을 요청합니다.  
			- 연결 유지형 API입니다. 연결 후 알림이 도착하면 서버에서 즉시 push 전송합니다.  
			- 연결 성공 시 `"connect"` 이벤트가 먼저 전송되며, 이후 `"notification"` 이벤트로 알림이 도착합니다.  
			- `produces = text/event-stream` 헤더 필요
			"""
	)
	@GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return sseService.connect(userDetails.getMember().getId());
	}
}

