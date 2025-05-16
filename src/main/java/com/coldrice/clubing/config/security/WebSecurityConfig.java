package com.coldrice.clubing.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.filter.JwtAuthenticationFilter;
import com.coldrice.clubing.filter.JwtAuthorizationFilter;
import com.coldrice.clubing.jwt.JwtUtil;
import com.coldrice.clubing.util.ResponseBodyDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;
	private final AuthenticationConfiguration authenticationConfiguration;
	private final MemberRepository memberRepository;
	private final CorsConfigurationSource corsConfigurationSource;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, memberRepository);
		filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
		return filter;
	}

	@Bean
	public JwtAuthorizationFilter jwtAuthorizationFilter() {
		return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 설정
		http.csrf((csrf) -> csrf.disable());

		// 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
		http.sessionManagement((sessionManagement) ->
			sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		);

		// 🔹 CORS 설정 적용
		http.cors(cors -> cors.configurationSource(corsConfigurationSource));

		http.authorizeHttpRequests((authorizeHttpRequests) ->
			authorizeHttpRequests
				.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 접근 허용 설정
				.requestMatchers("/").permitAll() // 메인 페이지 요청 허가
				.requestMatchers("/api/auth/**").permitAll() // '/api/user/'로 시작하는 요청 모두 접근 허가
				.requestMatchers(
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/swagger-resources/**",
					"/webjars/**",
					"/api/crawler/**"
				).permitAll() // Swagger 관련 요청 허용
				.anyRequest().authenticated() // 그 외 모든 요청 인증처리
		);

		// 사용자 권한 관리
		http.exceptionHandling(eh -> eh
			.accessDeniedHandler((request, response, accessDeniedException) -> {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json;charset=UTF-8");

				ResponseBodyDto<Object> errorResponse = ResponseBodyDto.fail("접근 권한이 없습니다.", null);
				String json = new ObjectMapper().writeValueAsString(errorResponse);

				response.getWriter().write(json);
			})
		);

		// 필터 관리
		http.addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);
		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}