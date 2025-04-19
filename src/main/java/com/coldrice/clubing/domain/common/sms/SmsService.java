package com.coldrice.clubing.domain.common.sms;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsService {

	private final SmsSender smsSender;
	private final RedisTemplate<String, String> redisTemplate;

	public void sendVerificationCode(String phoneNumber) {

		// 인증코드 생성 및 저장 (Redis)
		String code = generateCode();
		redisTemplate.opsForValue().set("sms:" + phoneNumber, code, 5, TimeUnit.MINUTES);

		// SMS 전송
		smsSender.sendSms(phoneNumber, code);
	}

	public boolean verifyCode(String phoneNumber, String inputCode) {
		String key = "sms:" + phoneNumber;
		String savedCode = redisTemplate.opsForValue().get(key);

		return inputCode.equals(savedCode);
	}

	private String generateCode() {
		return String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리 숫자
	}

}
