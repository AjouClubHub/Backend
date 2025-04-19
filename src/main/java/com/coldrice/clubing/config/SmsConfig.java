package com.coldrice.clubing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;

@Configuration
public class SmsConfig {

	@Bean
	public DefaultMessageService defaultMessageService(
		@Value("${coolsms.api.key}") String apiKey,
		@Value("${coolsms.api.secret}") String apiSecret
	) {
		return NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
	}
}
