package com.coldrice.clubing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Clubing API Docs")
				.version("1.0")
				.description("아주대학교 동아리 플랫폼 Clubing의 API 명세서입니다."));
	}
}
