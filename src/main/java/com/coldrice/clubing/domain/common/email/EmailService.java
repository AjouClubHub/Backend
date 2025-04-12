package com.coldrice.clubing.domain.common.email;

import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String content) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(content);
		message.setFrom("coldrice99@gmail.com");
		mailSender.send(message);
	}

	public String sendAuthCode(String email) {
		String code = generateCode();
		String content = "[Clubing] 인증번호: " + code;

		sendEmail(email,"아주대학교 Clubing 이메일 인증", content);

		return code;
	}

	private String generateCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000); // 6자리
		return String.valueOf(code);
	}
}
