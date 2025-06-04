package com.coldrice.clubing.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.auth.dto.LoginRequest;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

	private JwtUtil jwtUtil;
	private MemberRepository memberRepository;
	private AuthenticationManager authenticationManager;
	private TestableJwtAuthenticationFilter filter;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		jwtUtil = mock(JwtUtil.class);
		memberRepository = mock(MemberRepository.class);
		authenticationManager = mock(AuthenticationManager.class);

		// TestableJwtAuthenticationFilter 생성 후,
		// 외부에서 모의 AuthenticationManager 주입
		filter = new TestableJwtAuthenticationFilter(jwtUtil, memberRepository);
		filter.setAuthenticationManager(authenticationManager);

		objectMapper = new ObjectMapper();

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
	void attemptAuthentication_shouldReturnAuthentication_whenCredentialsAreValid() throws Exception {
		// --- 준비 ---
		// 1) 요청 바디에 들어갈 JSON 생성
		LoginRequest loginRequest = new LoginRequest("alice@ajou.ac.kr", "password123");
		String json = objectMapper.writeValueAsString(loginRequest);

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setContentType("application/json");
		req.setContent(json.getBytes(StandardCharsets.UTF_8));

		MockHttpServletResponse resp = new MockHttpServletResponse();

		// 2) memberRepository.findByEmail(...)이 Optional.of(member)를 반환하도록 스텁
		Member dummyMember = Member.builder()
			.id(100L)
			.email("alice@ajou.ac.kr")
			.password("encodedPwd")
			.name("Alice")
			.major("소프트웨어학과")
			.studentId("202300001")
			.memberRole(null) // role은 실제 검증에는 쓰이지 않으므로 null 처리
			.build();
		when(memberRepository.findByEmail("alice@ajou.ac.kr"))
			.thenReturn(Optional.of(dummyMember));

		// 3) authenticationManager.authenticate(...)이 정상 Authentication 객체를 반환하도록 스텁
		UsernamePasswordAuthenticationToken tokenPassed = new UsernamePasswordAuthenticationToken(
			"alice@ajou.ac.kr", "password123");
		Authentication mockAuthResult = new UsernamePasswordAuthenticationToken(
			new Object(), null, null
		);
		when(authenticationManager.authenticate(
			argThat(arg -> {
				//  전달된 토큰 내부의 이메일/패스워드가 예상과 일치하는지 확인
				UsernamePasswordAuthenticationToken t = (UsernamePasswordAuthenticationToken)arg;
				return "alice@ajou.ac.kr".equals(t.getPrincipal())
					&& "password123".equals(t.getCredentials());
			})
		)).thenReturn(mockAuthResult);

		// --- 실행 ---
		Authentication result = filter.attemptAuthentication(req, resp);

		// --- 검증 ---
		assertThat(result).isSameAs(mockAuthResult);
	}

	@Test
	void attemptAuthentication_shouldThrow_whenEmailNotFound() throws Exception {
		// --- 준비 ---
		LoginRequest loginRequest = new LoginRequest("unknown@ajou.ac.kr", "anyPwd");
		String json = objectMapper.writeValueAsString(loginRequest);

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setContentType("application/json");
		req.setContent(json.getBytes(StandardCharsets.UTF_8));

		MockHttpServletResponse resp = new MockHttpServletResponse();

		// memberRepository.findByEmail(...)이 빈 Optional을 반환하도록 스텁
		when(memberRepository.findByEmail("unknown@ajou.ac.kr"))
			.thenReturn(Optional.empty());

		// --- 실행/검증 ---
		assertThatThrownBy(() -> filter.attemptAuthentication(req, resp))
			.isInstanceOf(UsernameNotFoundException.class)
			.hasMessageContaining("존재하지 않는 유저 이메일입니다.");
	}

	@Test
	void attemptAuthentication_shouldThrowRuntimeException_onJsonParseError() {
		// --- 준비 ---
		// 잘못된 JSON 바디 (LoginRequest로 파싱할 수 없는 형태)
		String badJson = "{ \"email\": \"alice@ajou\", \"password\": 123 "; // 일부러 중괄호 누락
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.setContentType("application/json");
		req.setContent(badJson.getBytes(StandardCharsets.UTF_8));

		MockHttpServletResponse resp = new MockHttpServletResponse();

		// --- 실행/검증 ---
		assertThatThrownBy(() -> filter.attemptAuthentication(req, resp))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Unexpected end-of-input"); // Jackson 예외 메시지 일부 포함
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
