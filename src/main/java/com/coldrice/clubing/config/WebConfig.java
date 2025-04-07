package com.coldrice.clubing.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 전체 경로 허용
			.allowedOrigins("http://localhost:5173") // React 주소
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
			.allowCredentials(true);
	}
}

