package com.coldrice.clubing.domain.common.email;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailCodeManager {

	private static final long CODE_TIMEOUT = 5; // 인증코드 유효시간 (분)
	private static final long VERIFIED_TIMEOUT = 30; // 인증 완료 후 유효시간 (분)
	private final RedisTemplate<String, String> redisTemplate;

	private String codeKey(String email) {
		return "emailCode: " + email;
	}

	private String verifiedKey(String email) {
		return "emailVerified: " + email;
	}

	// 인증 코드 저장
	public void saveAuthCode(String email, String code) {
		redisTemplate.opsForValue().set(codeKey(email), code, CODE_TIMEOUT, TimeUnit.MINUTES);
	}

	// 인증 성공 시 -> 인증 상태 저장
	public boolean verifyCode(String email, String code) {
		String stored = redisTemplate.opsForValue().get(codeKey(email));
		if (stored != null && stored.equals(code)) {
			saveVerified(email);
			redisTemplate.delete(codeKey(email));
			return true;
		}

		return false;
	}

	// 인증 완료 상태 저장
	public void saveVerified(String email) {
		redisTemplate.opsForValue().set(verifiedKey(email), "true", VERIFIED_TIMEOUT, TimeUnit.MINUTES);
	}

	// 인증 여부 확인 (회원가입 시 호출)
	public boolean isVerified(String email) {
		String verified = redisTemplate.opsForValue().get(verifiedKey(email));
		return "true".equals(verified);
	}

	// 회원가입 완료 시 인증 정보 삭제
	public void clearVerified(String email) {
		redisTemplate.delete(verifiedKey(email));
	}

}
