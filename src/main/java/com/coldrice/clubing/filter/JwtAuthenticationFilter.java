package com.coldrice.clubing.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.auth.dto.LoginRequest;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final JwtUtil jwtUtil;
	private MemberRepository memberRepository;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, MemberRepository memberRepository) {
		this.jwtUtil = jwtUtil;
		this.memberRepository = memberRepository;
		setFilterProcessesUrl("/api/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		log.info("로그인 시도");
		try {
			LoginRequest requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

			Member member = memberRepository.findByEmail(requestDto.email())
				.orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저 이메일입니다."));

			// if (member.getDeletedAt() != null) {
			// 	throw new GlobalException(DELETED_USER);
			// }

			return getAuthenticationManager().authenticate(
				new UsernamePasswordAuthenticationToken(
					requestDto.email(),
					requestDto.password(),
					null
				)
			);
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) {
		log.info("로그인 성공 및 JWT 생성");
		String username = ((UserDetailsImpl)authResult.getPrincipal()).getUsername();
		MemberRole role = ((UserDetailsImpl)authResult.getPrincipal()).getMember().getMemberRole();
		Long userId = (((UserDetailsImpl)authResult.getPrincipal()).getMember().getId());

		String token = jwtUtil.createToken(userId, username, role);
		jwtUtil.addJwtToCookie(token, response);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) {
		log.info("로그인 실패");
		response.setStatus(401); // 실패했을 떄 여기를 활용하여 응답 만들기
	}

}
