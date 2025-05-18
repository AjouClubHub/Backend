package com.coldrice.clubing.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.coldrice.clubing.domain.notification.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SseService {

	private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
	private static final Long TIMEOUT = 60L * 1000 * 10; // 10분

	public SseEmitter connect(Long memberId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT);
		emitterMap.put(memberId, emitter);

		emitter.onCompletion(() -> emitterMap.remove(memberId));
		emitter.onTimeout(() -> emitterMap.remove(memberId));
		emitter.onError((e) -> emitterMap.remove(memberId));

		// 더미 데이터 전송 (연결 확인용)
		try {
			emitter.send(SseEmitter.event().name("connect").data("connected"));
		} catch (IOException e) {
			emitterMap.remove(memberId);
		}

		// 주기적인 heartbeat 메세지 전송
		startHeartbeat(emitter,memberId);

		return emitter;
	}

	public void sendNotification(Long memberId, NotificationResponse response) {
		SseEmitter emitter = emitterMap.get(memberId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name("notification")
					.data(response));
			} catch (IOException e) {
				emitterMap.remove(memberId);
			}
		}
	}

	private void startHeartbeat(SseEmitter emitter, Long memberId) {
		new Thread(() -> {
			try {
				while(true) {
					Thread.sleep(30000); // 30초
					emitter.send(SseEmitter.event().comment("heartbeat")); // 주석 전송
				}
			} catch (Exception e) {
				emitterMap.remove(memberId); // 클라이언트가 끊긴 경우 정리
			}
		}).start();
	}
}
