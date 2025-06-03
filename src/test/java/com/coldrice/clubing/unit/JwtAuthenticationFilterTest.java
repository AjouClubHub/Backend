package com.coldrice.clubing.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.jwt.JwtUtil;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

	private JwtUtil jwtUtil;
	private MemberRepository memberRepository;
	private TestableJwtAuthenticationFilter filter;

	@BeforeEach
	void setUp() {
		jwtUtil = mock(JwtUtil.class);
		memberRepository = mock(MemberRepository.class);

		// 테스트용 서브클래스 인스턴스 생성
		filter = new TestableJwtAuthenticationFilter(jwtUtil, memberRepository);

		// JwtUtil.addJwtToHeader(token, response) 호출 시,
		// 응답 헤더에 "Authorization: Bearer <token>" 을 붙이도록 설정
		doAnswer(invocation -> {
			String token = invocation.getArgument(0, String.class);
			MockHttpServletResponse resp = invocation.getArgument(1, MockHttpServletResponse.class);
			resp.addHeader("Authorization", "Bearer " + token);
			return null;
		}).when(jwtUtil).addJwtToHeader(anyString(), any(MockHttpServletResponse.class));
	}

	@Test
	void successfulAuthentication_shouldAddJwtToHeaderAndBody() throws IOException {
		// 1) 가짜 Member와 UserDetailsImpl 준비
		Member dummy = Member.builder()
			.id(42L)
			.email("test@ajou.ac.kr")
			.memberRole(MemberRole.MEMBER)
			.password("pw")
			.name("테스트")
			.major("공대")
			.studentId("20230000")
			.build();
		UserDetailsImpl ud = new UserDetailsImpl(dummy);

		// Authentication 결과 객체 생성
		Authentication authResult = new UsernamePasswordAuthenticationToken(
			ud, null, ud.getAuthorities()
		);

		// 2) JwtUtil.createToken(...) 호출 시 “fake-jwt”를 반환하도록 설정
		when(jwtUtil.createToken(eq(42L), eq("test@ajou.ac.kr"), eq(MemberRole.MEMBER)))
			.thenReturn("fake-jwt");

		// 3) 요청/응답 객체 준비
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		// 4) protected 메서드를 public 래퍼로 호출
		filter.exposeSuccessfulAuthentication(req, res, chain, authResult);

		// 5) 헤더에 “Authorization: Bearer fake-jwt”가 추가되었는지 확인
		assertThat(res.getHeader("Authorization")).isEqualTo("Bearer fake-jwt");

		// 6) 응답 바디에도 JSON 형태로 토큰이 실렸는지 확인
		String body = res.getContentAsString();
		assertThat(res.getContentType()).isEqualTo("application/json;charset=UTF-8");
		assertThat(body).contains("\"token\":\"fake-jwt\"");
	}

	@Test
	void unsuccessfulAuthentication_shouldReturn401() {
		// 1) 가짜 예외
		AuthenticationException ex = new AuthenticationException("로그인 실패") {
		};
		MockHttpServletRequest req = new MockHttpServletRequest();
		MockHttpServletResponse res = new MockHttpServletResponse();

		// 2) protected 메서드를 public 래퍼로 호출
		filter.exposeUnsuccessfulAuthentication(req, res, ex);

		// 3) 응답 상태가 401인가 확인
		assertThat(res.getStatus()).isEqualTo(401);
	}
}
