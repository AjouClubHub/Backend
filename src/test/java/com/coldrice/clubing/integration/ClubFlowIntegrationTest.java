package com.coldrice.clubing.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.coldrice.clubing.domain.announcement.repository.AnnouncementRepository;
import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.common.sms.dto.SmsVerificationRequest;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;
import com.coldrice.clubing.domain.schedule.repository.ScheduleRepository;
import com.coldrice.clubing.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test") // test profile을 명시해야 application-test.yml이 반영됨
public class ClubFlowIntegrationTest {

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	static Member user;
	static Member manager;
	static String userToken;
	static String managerToken;

	static Long clubId;
	static Long applicationId;

	@BeforeAll
	static void setupShared(
		@Autowired MemberRepository memberRepository,
		@Autowired ClubRepository clubRepository,
		@Autowired JwtUtil jwtUtil
	) {
		user = Member.builder()
			.id(1L)
			.name("일반 사용자")
			.email("user@ajou.ac.kr")
			.password("encodedPwd")
			.major("사이버보안학과")
			.studentId("202312345")
			.memberRole(MemberRole.MEMBER)
			.build();

		manager = Member.builder()
			.id(2L)
			.name("클럽 관리자")
			.email("manager@ajou.ac.kr")
			.password("encodedPwd")
			.major("소프트웨어학과")
			.studentId("202398765")
			.memberRole(MemberRole.MANAGER)
			.build();

		user = memberRepository.save(user);
		manager = memberRepository.save(manager);

		// JWT 발급
		userToken = "Bearer " + jwtUtil.createToken(1L, user.getEmail(), user.getMemberRole());
		managerToken = "Bearer " + jwtUtil.createToken(2L, manager.getEmail(), manager.getMemberRole());

		Club club = Club.builder()
			.name("SMS 인증 테스트 클럽")
			.status(ClubStatus.APPROVED)
			.manager(manager)
			.contactInfo("010-8968-8493")
			.requiredMajors(Collections.emptyList())
			.description("테스트용 클럽")
			.keyword("테스트")
			.location("온라인")
			.type(ClubType.동아리)
			.build();

		clubId = clubRepository.save(club).getId();
	}

	@AfterAll
	static void cleanup(
		@Autowired MemberRepository memberRepository,
		@Autowired ClubRepository clubRepository,
		@Autowired ApplicationRepository applicationRepository,
		@Autowired MembershipRepository membershipRepository,
		@Autowired AnnouncementRepository announcementRepository,
		@Autowired ScheduleRepository scheduleRepository,
		@Autowired NotificationRepository notificationRepository) {
		// 참조하는 쪽 먼저 삭제
		scheduleRepository.deleteAll();
		announcementRepository.deleteAll();
		notificationRepository.deleteAll();
		applicationRepository.deleteAll();
		membershipRepository.deleteAll();
		clubRepository.deleteAll();
		memberRepository.deleteAll();
	}

	@Test
	@Order(1)
	void step1_승인된_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value("승인된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@Order(2)
	void step2_클럽_가입_신청_일반회원() throws Exception {

		ApplicationRequest request = new ApplicationRequest(
			"2000-05-24",
			"여자",
			"010-1234-5678",
			"열심히 활동하고 싶습니다!",
			user.getStudentId()
		);

		// when & then
		mockMvc.perform(post("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 성공"))
			.andExpect(jsonPath("$.data.status").value("PENDING"));

		applicationId = applicationRepository.findAll().get(0).getId();
	}

	// @Test
	// @Order(3)
	// void step3_클럽_관리자_인증코드_요청() throws Exception {
	//
	// 	mockMvc.perform(post("/api/clubs/{clubId}/manager-auth/request", clubId)
	// 			.header("Authorization", managerToken)
	// 			.contentType(MediaType.APPLICATION_JSON)
	// 			.content("""
	// 				    { "phoneNumber": "010-8968-8493" }
	// 				"""))
	// 		.andExpect(status().isOk())
	// 		.andExpect(jsonPath("$.message").value("인증 코드 전송 완료"));
	// }

	@Test
	@Order(4)
	void step4_클럽_관리자_인증_성공() throws Exception {

		// 인증 코드 Redis에 저장
		redisTemplate.opsForValue().set("sms:010-8968-8493", "123456");

		SmsVerificationRequest request = new SmsVerificationRequest("010-8968-8493", "123456");

		mockMvc.perform(patch("/api/clubs/{clubId}/manager-auth/verify", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 관리자 인증 완료"));
	}

	@Test
	@Order(5)
	void step5_가입_신청_목록_조회_관리자() throws Exception {

		// when & then: 관리자(권한: MANAGER)가 해당 클럽의 신청 목록 조회
		mockMvc.perform(get("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@Order(6)
	void step6_가입_승인_관리자() throws Exception {
		// given: 승인 요청 DTO
		String requestBody = """
			    {
			        "status": "APPROVED"
			    }
			""";

		// when & then: 승인 API 호출
		mockMvc.perform(put("/api/clubs/{clubId}/applications/{applicationId}/approval", clubId, applicationId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 승인/거절이 완료되었습니다"));
	}

	@Test
	@Order(7)
	void step7_공지사항_등록_관리자() throws Exception {
		// given
		String requestBody = """
			    {
			        "title": "MT 모집 공지",
			        "content": "5월에 MT를 떠납니다. 참가자 모집합니다!",
			        "category": "모임"
			    }
			""";

		// when & then
		mockMvc.perform(post("/api/clubs/{clubId}/announcements", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 등록 완료"))
			.andExpect(jsonPath("$.data.title").value("MT 모집 공지"))
			.andExpect(jsonPath("$.data.content").value("5월에 MT를 떠납니다. 참가자 모집합니다!"))
			.andExpect(jsonPath("$.data.category").value("모임"));
	}

	@Test
	@Order(8)
	void step8_일정_등록_관리자() throws Exception {
		// given
		String requestBody = """
			    {
			        "title": "신입생 환영회",
			        "content": "5월 30일 오후 2시부터 체육관에서 신입생 환영회가 진행됩니다.",
			        "startTime": "2025-05-30T14:00:00",
			        "endTime": "2025-05-30T17:00:00"
			    }
			""";

		// when & then
		mockMvc.perform(post("/api/clubs/{clubId}/schedules", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("일정 등록 완료"))
			.andExpect(jsonPath("$.data.title").value("신입생 환영회"))
			.andExpect(jsonPath("$.data.content").value("5월 30일 오후 2시부터 체육관에서 신입생 환영회가 진행됩니다."))
			.andExpect(jsonPath("$.data.startTime").exists())
			.andExpect(jsonPath("$.data.endTime").exists());
	}

	@Test
	@Order(9)
	void step9_내가_가입한_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/my/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("내 가입된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].clubId").value(clubId));
	}

	@Test
	@Order(10)
	void step10_공지사항_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/announcements", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].title").value("MT 모집 공지"))
			.andExpect(jsonPath("$.data[0].content").value("5월에 MT를 떠납니다. 참가자 모집합니다!"))
			.andExpect(jsonPath("$.data[0].category").value("모임"));
	}

	@Test
	@Order(11)
	void step11_일정_조회_일반회원() throws Exception {
		// given: 현재 ~ 1달 뒤까지 조회
		String start = "2025-05-01T00:00:00";
		String end = "2025-06-30T23:59:59";

		// when & then
		mockMvc.perform(get("/api/clubs/{clubId}/schedules", clubId)
				.param("start", start)
				.param("end", end)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("일정 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].title").value("신입생 환영회"))
			.andExpect(jsonPath("$.data[0].content").value("5월 30일 오후 2시부터 체육관에서 신입생 환영회가 진행됩니다."));
	}
}
