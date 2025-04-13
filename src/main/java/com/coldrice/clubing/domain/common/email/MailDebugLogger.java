package com.coldrice.clubing.domain.common.email;

import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailDebugLogger implements CommandLineRunner {

	private final JavaMailSender javaMailSender;

	@Override
	public void run(String... args) {
		JavaMailSenderImpl sender = (JavaMailSenderImpl) javaMailSender;
		System.out.println("✅ 메일 설정 확인:");
		System.out.println("➡ Host = " + sender.getHost()); // localhost 라면 문제 있음
		System.out.println("➡ Port = " + sender.getPort());
		System.out.println("➡ Username = " + sender.getUsername());
	}
}

