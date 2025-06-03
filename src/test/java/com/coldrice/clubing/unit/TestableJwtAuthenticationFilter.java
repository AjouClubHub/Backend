package com.coldrice.clubing.unit;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.filter.JwtAuthenticationFilter;
import com.coldrice.clubing.jwt.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter의 protected 메서드를
 * 테스트 패키지(com.coldrice.clubing.unit)에서 호출할 수 있도록
 * public 래퍼를 제공하는 서브클래스입니다.
 */
public class TestableJwtAuthenticationFilter extends JwtAuthenticationFilter {

	public TestableJwtAuthenticationFilter(JwtUtil jwtUtil, MemberRepository memberRepository) {
		super(jwtUtil, memberRepository);
	}

	/**
	 * 원래 protected인 successfulAuthentication()을
	 * public으로 래핑해서 노출합니다.
	 */
	public void exposeSuccessfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain chain,
		Authentication authResult
	) throws IOException {
		super.successfulAuthentication(request, response, chain, authResult);
	}

	/**
	 * 원래 protected인 unsuccessfulAuthentication()을
	 * public으로 래핑해서 노출합니다.
	 */
	public void exposeUnsuccessfulAuthentication(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException failed
	) {
		super.unsuccessfulAuthentication(request, response, failed);
	}
}
