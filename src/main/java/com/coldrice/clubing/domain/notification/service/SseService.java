package com.coldrice.clubing.domain.notification.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.coldrice.clubing.domain.notification.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

	private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();
	private static final Long TIMEOUT = 0L; // 연결 무제한 유지

	public SseEmitter connect(Long memberId) {
		log.info("SSE 연결 요청 수신: memberId={}", memberId);

		SseEmitter emitter = new SseEmitter(TIMEOUT);
		emitterMap.put(memberId, emitter);

		emitter.onCompletion(() -> {
			log.info("SSE 연결 종료됨: memberId={}", memberId);
			emitterMap.remove(memberId);
		});
		emitter.onTimeout(() -> {
			log.info("SSE 연결 타임아웃: memberId={}", memberId);
			emitterMap.remove(memberId);
		});
		emitter.onError((e) -> {
			log.error("SSE 에러 발생: {}", e.getMessage());
			emitterMap.remove(memberId);
		});

		// 더미 데이터 전송 (연결 확인용)
		try {
			emitter.send(SseEmitter.event().name("connect").data("connected"));
			log.info("연결 이벤트 전송 완료");
		} catch (IOException e) {
			log.error("연결 이벤트 전송 실패: {}", e.getMessage());
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
					emitter.send(SseEmitter.event().comment("heartbeat")); // 30초마다 빈 주석 전송
				}
			} catch (Exception e) {
				emitterMap.remove(memberId); // 클라이언트가 끊긴 경우 정리
			}
		}).start();
	}
}
