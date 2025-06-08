package com.coldrice.clubing.integration;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.coldrice.clubing.domain.announcement.dto.AnnouncementRequest;
import com.coldrice.clubing.domain.announcement.entity.Announcement;
import com.coldrice.clubing.domain.announcement.entity.AnnouncementCategory;
import com.coldrice.clubing.domain.announcement.repository.AnnouncementRepository;
import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.repository.ApplicationRepository;
import com.coldrice.clubing.domain.auth.dto.EmailVerificationRequest;
import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.common.email.EmailCodeManager;
import com.coldrice.clubing.domain.common.sms.SmsService;
import com.coldrice.clubing.domain.common.sms.dto.SmsVerificationRequest;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.notification.repository.NotificationRepository;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentRequest;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentUpdateRequest;
import com.coldrice.clubing.domain.recruitment.repository.RecruitmentRepository;
import com.coldrice.clubing.domain.schedule.repository.ScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles("test")
public class ClubFlowIntegrationTest {

	private static Member user;
	private static Member manager;
	private static String userToken;
	private static String managerToken;
	private static Long clubId;
	private static Long applicationId;
	private static Long announcementId;
	private static Long recruitmentId;

	@Autowired
	MockMvc mockMvc;
	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	private ApplicationRepository applicationRepository;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	@Autowired
	private AnnouncementRepository announcementRepository;
	@Autowired
	private EmailCodeManager emailCodeManager;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ClubRepository clubRepository;
	@MockBean
	private SmsService smsService;

	@BeforeAll
	static void setupShared(
		@Autowired ClubRepository clubRepository
	) {
		Club club = Club.builder()
			.name("SMS 인증 테스트 클럽")
			.status(ClubStatus.APPROVED)
			.manager(null) // manager는 나중에 등록됩니다
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
		@Autowired RecruitmentRepository recruitmentRepository,
		@Autowired ScheduleRepository scheduleRepository,
		@Autowired NotificationRepository notificationRepository) {

		scheduleRepository.deleteAll();
		recruitmentRepository.deleteAll();
		announcementRepository.deleteAll();
		notificationRepository.deleteAll();
		applicationRepository.deleteAll();
		membershipRepository.deleteAll();
		clubRepository.deleteAll();
		memberRepository.deleteAll();
	}

	// ─── STEP 1: 일반회원 이메일 인증 & 회원가입 ─────────────────────────────────
	@Test
	@Order(1)
	void step1_이메일_인증_및_회원가입_일반회원() throws Exception {
		String email = "user@ajou.ac.kr";

		// 1-1) 이메일 코드 저장
		emailCodeManager.saveAuthCode(email, "CODE123");

		// 1-2) 이메일 검증 API 호출
		mockMvc.perform(post("/api/auth/verify-email")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new EmailVerificationRequest(email, "CODE123")
				)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 인증 성공"));

		// 1-3) 회원가입 요청
		SignupRequest signupRequest = new SignupRequest(
			"일반 사용자",    // name
			email,           // email
			"password123",   // password
			"사이버보안학과",   // major
			"MEMBER",        // memberRole
			"202312345"      // studentId
		);

		// 1-4) 회원가입 테스트
		String responseBody = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("회원가입 성공"))
			.andExpect(jsonPath("$.data.bearerToken").exists())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// 1-5) DB에 Member 저장 여부 확인
		user = memberRepository.findByEmail(email).orElseThrow();
		assertThat(user.getStudentId()).isEqualTo("202312345");

		// 1-6) JSON에서 bearerToken을 그대로 꺼내서 userToken에 저장 (prefix 추가 금지)
		String rawToken = objectMapper
			.readTree(responseBody)
			.get("data")
			.get("bearerToken")
			.asText();
		userToken = "Bearer " + rawToken;
	}

	// ─── STEP 2: 관리자 이메일 인증 & 회원가입 ─────────────────────────────────
	@Test
	@Order(2)
	void step2_이메일_인증_및_회원가입_관리자() throws Exception {
		String email = "manager@ajou.ac.kr";

		// 2-1) 이메일 코드 저장
		emailCodeManager.saveAuthCode(email, "CODE456");

		// 2-2) 이메일 검증 API 호출
		mockMvc.perform(post("/api/auth/verify-email")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(
					new EmailVerificationRequest(email, "CODE456")
				)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 인증 성공"));

		// 2-3) 관리자 회원가입 요청
		SignupRequest signupRequest = new SignupRequest(
			"클럽 관리자",    // name
			email,           // email
			"password456",   // password
			"소프트웨어학과",  // major
			"MANAGER",       // memberRole
			"202398765"      // studentId
		);

		// 2-4) 관리자 회원가입 테스트
		String responseBody = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("회원가입 성공"))
			.andExpect(jsonPath("$.data.bearerToken").exists())
			.andReturn()
			.getResponse()
			.getContentAsString();

		// 2-5) DB에서 Manager Role 확인
		manager = memberRepository.findByEmail(email).orElseThrow();
		assertThat(manager.getMemberRole()).isEqualTo(MemberRole.MANAGER);

		// 2-6) JSON에서 bearerToken을 그대로 꺼내서 managerToken에 저장
		String rawToken = objectMapper
			.readTree(responseBody)
			.get("data")
			.get("bearerToken")
			.asText();
		managerToken = "Bearer " + rawToken;

		// 2-7) (선택사항) Club 엔티티의 manager 필드 업데이트
		Club existingClub = clubRepository.findById(clubId).orElseThrow();
		existingClub.updateManager(manager);
		clubRepository.save(existingClub);
	}

	// ─── STEP 3: 이메일 중복 체크 ───────────────────────────────────────────
	@Test
	@Order(3)
	void step3_이메일_중복_체크() throws Exception {
		// 이미 가입된 이메일인 경우 (user@ajou.ac.kr → true)
		mockMvc.perform(get("/api/auth/email")
				.param("email", "user@ajou.ac.kr"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 중복 여부 조회 완료"))
			.andExpect(jsonPath("$.data").value(true));

		// 가입되지 않은 이메일인 경우 (new@ajou.ac.kr → false)
		mockMvc.perform(get("/api/auth/email")
				.param("email", "new@ajou.ac.kr"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("이메일 중복 여부 조회 완료"))
			.andExpect(jsonPath("$.data").value(false));
	}

	// ─── STEP 4: 학번 중복 체크 ────────────────────────────────────────────
	@Test
	@Order(4)
	void step4_학번_중복_체크() throws Exception {
		// 이미 가입된 학번인 경우 ("202312345" → true)
		mockMvc.perform(get("/api/auth/studentId")
				.param("studentId", "202312345"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("학번 중복 여부 확인"))
			.andExpect(jsonPath("$.data").value(true));

		// 가입되지 않은 학번인 경우 ("999999999" → false)
		mockMvc.perform(get("/api/auth/studentId")
				.param("studentId", "999999999"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("학번 중복 여부 확인"))
			.andExpect(jsonPath("$.data").value(false));
	}

	// ─── STEP 5: 클럽 관리자 인증코드 요청 ──────────────────────────────────
	@Test
	@Order(5)
	void step5_클럽_관리자_인증코드_요청() throws Exception {
		// SmsService.sendVerificationCode(...)가 아무 동작도 하지 않도록 스텁 처리
		doNothing().when(smsService).sendVerificationCode("010-8968-8493");

		mockMvc.perform(post("/api/clubs/{clubId}/manager-auth/request", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					    { "phoneNumber": "010-8968-8493" }
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("인증 코드 전송 완료"));
	}

	// ─── STEP 6: 클럽 관리자 인증(휴대폰) 테스트 ─────────────────────────────
	@Test
	@Order(6)
	void step6_클럽_관리자_인증_성공() throws Exception {
		// Redis에 인증 코드 미리 저장 (테스트용 고정값)
		redisTemplate.opsForValue().set("sms:010-8968-8493", "123456");

		// SmsService.verifyCode(...)를 무조건 true 반환하도록 설정
		when(smsService.verifyCode("010-8968-8493", "123456")).thenReturn(true);

		// 클럽 관리자 인증 API 호출
		SmsVerificationRequest request = new SmsVerificationRequest("010-8968-8493", "123456");
		mockMvc.perform(patch("/api/clubs/{clubId}/manager-auth/verify", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 관리자 인증 완료"));
	}

	// ─── STEP 7: 승인된 클럽 목록 조회 (일반회원) ────────────────────────────
	@Test
	@Order(7)
	void step7_승인된_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value("승인된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	// ─── STEP 8: 클럽 카테고리 검색 ────────────────────────────────────────
	@Test
	@Order(8)
	void step8_클럽_카테고리_검색_성공() throws Exception {
		mockMvc.perform(get("/api/clubs/search")
				.param("category", "운동")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 검색 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	// ─── STEP 9: 클럽 가입 신청 (일반회원) ──────────────────────────────────
	@Test
	@Order(9)
	void step9_클럽_가입_신청_일반회원() throws Exception {
		ApplicationRequest request = new ApplicationRequest(
			"2000-05-24",
			"여자",
			"010-1234-5678",
			"열심히 활동하고 싶습니다!",
			user.getStudentId()
		);

		mockMvc.perform(post("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 성공"))
			.andExpect(jsonPath("$.data.status").value("PENDING"));

		applicationId = applicationRepository.findAll().get(0).getId();
	}

	// ─── STEP 10: 내가 신청한 클럽 현황 조회 (일반회원) ─────────────────────────
	@Test
	@Order(10)
	void step10_내_클럽_가입_신청_현황_조회() throws Exception {
		mockMvc.perform(get("/api/my/applications")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 신청 현황 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].applicationId").value(applicationId))
			.andExpect(jsonPath("$.data[0].clubType").value("동아리"))
			.andExpect(jsonPath("$.data[0].clubName").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data[0].memberName").value("일반 사용자"))
			.andExpect(jsonPath("$.data[0].status").value("PENDING"));
	}

	// ─── STEP 11: 가입 신청 취소 및 재신청 ───────────────────────────────────
	@Test
	@Order(11)
	void step11_가입_신청_취소_및_재신청() throws Exception {
		// 11-1) 가입 신청 취소
		mockMvc.perform(delete("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 신청이 취소되었습니다."));

		assertThat(applicationRepository.findByClubIdAndMemberId(clubId, user.getId())).isEmpty();

		// 11-2) 동일 내용으로 재신청
		ApplicationRequest reapplyRequest = new ApplicationRequest(
			"2000-05-24",
			"여자",
			"010-1234-5678",
			"열심히 활동하고 싶습니다!",
			user.getStudentId()
		);

		mockMvc.perform(post("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(reapplyRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 성공"))
			.andExpect(jsonPath("$.data.status").value("PENDING"));

		applicationId = applicationRepository
			.findByClubIdAndMemberId(clubId, user.getId())
			.orElseThrow()
			.getId();
	}

	// ─── STEP 12: 가입 신청 목록 조회 (관리자) ─────────────────────────────────
	@Test
	@Order(12)
	void step12_가입_신청_목록_조회_관리자() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	// ─── STEP 13: 가입 신청 단건 조회 (관리자) ─────────────────────────────────
	@Test
	@Order(13)
	void step13_가입_신청_단건_조회_관리자() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/applications/{applicationId}", clubId, applicationId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 단건 조회 성공"))
			.andExpect(jsonPath("$.data.application.applicationId").value(applicationId))
			.andExpect(jsonPath("$.data.application.clubType").value("동아리"))
			.andExpect(jsonPath("$.data.application.clubName").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data.application.memberName").value("일반 사용자"))
			.andExpect(jsonPath("$.data.application.status").value("PENDING"))
			.andExpect(jsonPath("$.data.applicationInfo.birthDate").value("2000-05-24"))
			.andExpect(jsonPath("$.data.applicationInfo.studentId").value("여자"))
			.andExpect(jsonPath("$.data.applicationInfo.gender").value("010-1234-5678"))
			.andExpect(jsonPath("$.data.applicationInfo.phoneNumber").value("열심히 활동하고 싶습니다!"))
			.andExpect(jsonPath("$.data.applicationInfo.motivation").value(user.getStudentId()));
	}

	// ─── STEP 14: 가입 신청 단건 조회 권한 없음 ─────────────────────────────────
	@Test
	@Order(14)
	void step14_가입_신청_단건_조회_권한없음() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/applications/{applicationId}", clubId, applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isForbidden());
	}

	// ─── STEP 15: 가입 거절 (관리자) ───────────────────────────────────────────
	@Test
	@Order(15)
	void step15_가입_거절_관리자() throws Exception {
		String requestBody = """
			    {
			        "status": "REJECTED",
			        "rejectionReason": "정원이 초과되었습니다"
			    }
			""";

		mockMvc.perform(put("/api/clubs/{clubId}/applications/{applicationId}/approval", clubId, applicationId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 승인/거절이 완료되었습니다"));
	}

	// ─── STEP 16: 가입 거절 알림 확인 (일반회원) ───────────────────────────────
	@Test
	@Order(16)
	void step16_가입_거절_알림_확인_일반회원() throws Exception {
		mockMvc.perform(get("/api/notifications")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("알림 전체 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].content").value("SMS 인증 테스트 클럽에서 가입이 거절되었습니다."));
	}

	// ─── STEP 17: 거절 사유 정상 조회 ────────────────────────────────────────
	@Test
	@Order(17)
	void step17_거절_사유_정상_조회() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("거절 사유 조회 성공"))
			.andExpect(jsonPath("$.data.rejectionReason").value("정원이 초과되었습니다"));
	}

	// ─── STEP 18: 다른 사용자가 거절 사유 조회 시 예외 ───────────────────────
	@Test
	@Order(18)
	void step18_다른_사용자가_거절_사유_조회시_예외() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", managerToken))
			.andExpect(status().isUnauthorized());
	}

	// ─── STEP 19: 가입 승인 (관리자) ─────────────────────────────────────────
	@Test
	@Order(19)
	void step19_가입_승인_관리자() throws Exception {
		String requestBody = """
			    {
			        "status": "APPROVED"
			    }
			""";

		mockMvc.perform(put("/api/clubs/{clubId}/applications/{applicationId}/approval", clubId, applicationId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 승인/거절이 완료되었습니다"));
	}

	// ─── STEP 20: 거절되지 않은 신청 사유 조회 시 예외 ────────────────────────
	@Test
	@Order(20)
	void step20_거절되지_않은_신청_사유_조회시_예외() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isBadRequest());
	}

	// ─── STEP 21: 공지사항 등록 (관리자) ──────────────────────────────────────
	@Test
	@Order(21)
	void step21_공지사항_등록_관리자() throws Exception {
		String requestBody = """
			    {
			        "title": "MT 모집 공지",
			        "content": "5월에 MT를 떠납니다. 참가자 모집합니다!",
			        "category": "모임"
			    }
			""";

		mockMvc.perform(post("/api/clubs/{clubId}/announcements", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 등록 완료"))
			.andExpect(jsonPath("$.data.title").value("MT 모집 공지"))
			.andExpect(jsonPath("$.data.content").value("5월에 MT를 떠납니다. 참가자 모집합니다!"))
			.andExpect(jsonPath("$.data.category").value("모임"));

		Announcement ann = announcementRepository.findAll().get(0);
		announcementId = ann.getId();
	}

	// ─── STEP 22: 일정 등록 (관리자) ─────────────────────────────────────────
	@Test
	@Order(22)
	void step22_일정_등록_관리자() throws Exception {
		String requestBody = """
			    {
			        "title": "신입생 환영회",
			        "content": "5월 30일 오후 2시부터 체육관에서 신입생 환영회가 진행됩니다.",
			        "startTime": "2025-05-30T14:00:00",
			        "endTime": "2025-05-30T17:00:00"
			    }
			""";

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

	// ─── STEP 23: 내가 가입한 클럽 목록 조회 (일반회원) ─────────────────────────
	@Test
	@Order(23)
	void step23_내가_가입한_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/my/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("내 가입된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].clubId").value(clubId));
	}

	// ─── STEP 24: 공지사항 목록 조회 (일반회원) ─────────────────────────────────
	@Test
	@Order(24)
	void step24_공지사항_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/announcements", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].title").value("MT 모집 공지"))
			.andExpect(jsonPath("$.data[0].content").value("5월에 MT를 떠납니다. 참가자 모집합니다!"))
			.andExpect(jsonPath("$.data[0].category").value("모임"));
	}

	// ─── STEP 25: 일정 조회 (일반회원) ─────────────────────────────────────────
	@Test
	@Order(25)
	void step25_일정_조회_일반회원() throws Exception {
		String start = "2025-05-01T00:00:00";
		String end = "2025-06-30T23:59:59";

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

	// ─── STEP 26: 클럽 탈퇴 (일반회원) ───────────────────────────────────────
	@Test
	@Order(26)
	void step26_클럽_탈퇴_일반회원() throws Exception {
		String requestBody = """
			    {
			        "leaveReason": "개인 사정"
			    }
			""";

		mockMvc.perform(delete("/api/clubs/{clubId}/withdraw", clubId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 탈퇴가 완료되었습니다."));
	}

	// ─── STEP 27: 공지사항 수정 (관리자) ────────────────────────────────────
	@Test
	@Order(27)
	void step27_공지사항_수정_관리자() throws Exception {
		String url = "/api/clubs/{clubId}/announcements/{announcementId}";

		AnnouncementRequest request = new AnnouncementRequest(
			"Updated Title",
			"Updated Content",
			AnnouncementCategory.모임
		);

		mockMvc.perform(patch(url, clubId, announcementId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 수정 완료"))
			.andExpect(jsonPath("$.data.title").value("Updated Title"))
			.andExpect(jsonPath("$.data.content").value("Updated Content"));

		Announcement updated = announcementRepository.findById(announcementId).orElseThrow();
		assertThat(updated.getTitle()).isEqualTo("Updated Title");
		assertThat(updated.getContent()).isEqualTo("Updated Content");
	}

	// ─── STEP 28: 공지사항 수정 권한없음 ─────────────────────────────────────
	@Test
	@Order(28)
	void step28_공지사항_수정_권한없음() throws Exception {
		AnnouncementRequest request = new AnnouncementRequest(
			"Hacked Title",
			"Hacked Content",
			AnnouncementCategory.모임
		);

		mockMvc.perform(patch("/api/clubs/{clubId}/announcements/{announcementId}", clubId, announcementId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isForbidden());
	}

	// ─── STEP 29: 공지사항 수정 잘못된 클럽/공지 ID ────────────────────────────
	@Test
	@Order(29)
	void step29_공지사항_수정_잘못된_클럽_또는_공지() throws Exception {
		AnnouncementRequest request = new AnnouncementRequest(
			"New Title",
			"New Content",
			AnnouncementCategory.모임
		);

		mockMvc.perform(patch("/api/clubs/{clubId}/announcements/{announcementId}", clubId + 999, announcementId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	// ─── STEP 30: 공지사항 단건 조회 & 조회수 증가 ─────────────────────────────
	@Test
	@Order(30)
	void step30_공지사항_단건_조회_조회수_증가() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/announcements/{announcementId}", clubId, announcementId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("공지사항 단건 조회 성공"))
			.andExpect(jsonPath("$.data.id").value(announcementId))
			.andExpect(jsonPath("$.data.title").value("Updated Title"))
			.andExpect(jsonPath("$.data.content").value("Updated Content"))
			.andExpect(jsonPath("$.data.category").value("모임"))
			.andExpect(jsonPath("$.data.views").value(1));

		Announcement after = announcementRepository.findById(announcementId).orElseThrow();
		assertThat(after.getViews()).isEqualTo(1);
	}

	// ─── STEP 31: PENDING 상태의 클럽 목록 조회 ─────────────────────────────────
	@Test
	@Order(31)
	void step31_pendingClubs_관리자미인증목록조회() throws Exception {
		// given: PENDING 상태의 클럽을 하나 더 등록
		Club pending = Club.builder()
			.name("Pending Club")
			.status(ClubStatus.PENDING)
			.manager(null)
			.contactInfo("010-0000-0000")
			.requiredMajors(Collections.emptyList())
			.description("Pending club description")
			.keyword("pending")
			.location("온라인")
			.type(ClubType.동아리)
			.build();
		clubRepository.save(pending);

		// when & then
		mockMvc.perform(get("/api/clubs/pending")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("관리자 미인증 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].name").value("Pending Club"))
			.andExpect(jsonPath("$.data[0].status").value("PENDING"));
	}

	// ─── STEP 32: 클럽 단건 조회 ─────────────────────────────────
	@Test
	@Order(32)
	void step32_클럽_단건조회() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 단건 조회 성공"))
			.andExpect(jsonPath("$.data.id").value(clubId))
			.andExpect(jsonPath("$.data.name").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data.status").value("APPROVED"));
	}

	// ─── STEP 33: 마이페이지 조회 ───────────────────────────────────
	@Test
	@Order(33)
	void step33_마이페이지_조회() throws Exception {
		mockMvc.perform(get("/api/member/mypage")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("마이페이지 조회 성공"))
			// 프로필 확인
			.andExpect(jsonPath("$.data.member.name").value("일반 사용자"))
			.andExpect(jsonPath("$.data.member.email").value("user@ajou.ac.kr"))
			.andExpect(jsonPath("$.data.member.major").value("사이버보안학과"))
			.andExpect(jsonPath("$.data.member.studentId").value("202312345"))
			// 가입된 클럽(없음)
			.andExpect(jsonPath("$.data.joinedClubs").isArray())
			.andExpect(jsonPath("$.data.joinedClubs").isEmpty())
			// 가입 신청 현황(승인 하나)
			.andExpect(jsonPath("$.data.applications").isArray())
			.andExpect(jsonPath("$.data.applications[0].status").value("APPROVED"))
			// 알림(4건)
			.andExpect(jsonPath("$.data.notifications").isArray())
			.andExpect(jsonPath("$.data.notifications.length()").value(4));
	}

	// ─── STEP 34: 내가 가입한 클럽 단건 조회 ─────────────────────────────────
	@Test
	@Order(34)
	void step34_내가_가입한_클럽_단건_조회() throws Exception {
		mockMvc.perform(get("/api/my/clubs/{clubId}", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("내 가입된 클럽 상세 조회 성공"))
			.andExpect(jsonPath("$.data.clubId").value(clubId))
			.andExpect(jsonPath("$.data.clubName").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data.clubType").value("동아리"));
	}

	// ─── STEP 35: 클럽 회원 목록 조회 ───────────────────────────────────────
	@Test
	@Order(35)
	void step35_클럽_회원_목록_조회() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/members", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 회원 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].memberId").value(manager.getId()))
			.andExpect(jsonPath("$.data[0].name").value("클럽 관리자"));
	}

	// ─── STEP 36: 클럽 회원 단건 조회 ───────────────────────────────────────
	@Test
	@Order(36)
	void step36_클럽_회원_단건_조회() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/members/{memberId}", clubId, user.getId())
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 회원 단건 조회 성공"))
			// member 정보 검증
			.andExpect(jsonPath("$.data.member.memberId").value(user.getId()))
			.andExpect(jsonPath("$.data.member.name").value("일반 사용자"))
			.andExpect(jsonPath("$.data.member.memberRole").value("MEMBER"))
			.andExpect(jsonPath("$.data.applicationInfo.birthDate").value("2000-05-24"))
			.andExpect(jsonPath("$.data.applicationInfo.studentId").value("여자"))
			.andExpect(jsonPath("$.data.applicationInfo.gender").value("010-1234-5678"))
			.andExpect(jsonPath("$.data.applicationInfo.phoneNumber").value("열심히 활동하고 싶습니다!"))
			.andExpect(jsonPath("$.data.applicationInfo.motivation").value("202312345"));
	}

	// ─── STEP 37: 내가 관리중인 클럽 목록 조회 ─────────────────────────────────
	@Test
	@Order(37)
	void step37_관리중인_클럽_목록_조회() throws Exception {
		mockMvc.perform(get("/api/my/manage-clubs")
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("관리중인 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].id").value(clubId))
			.andExpect(jsonPath("$.data[0].memberCount").isNumber())
			.andExpect(jsonPath("$.data[0].pendingApplications").isNumber());
	}

	// ─── STEP 38: 내가 관리중인 클럽 단건 조회 ─────────────────────────────────
	@Test
	@Order(38)
	void step38_관리중인_클럽_단건_조회() throws Exception {
		mockMvc.perform(get("/api/my/manage-clubs/{clubId}", clubId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("관리중인 클럽 상세 조회 성공"))
			// ManagedClubResponse 의 실제 반환 필드명에 맞춰 검증
			.andExpect(jsonPath("$.data.id").value(clubId))
			.andExpect(jsonPath("$.data.name").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data.memberCount").isNumber())
			.andExpect(jsonPath("$.data.pendingApplications").isNumber())
			.andExpect(jsonPath("$.data.announcementCount").isNumber());
	}

	// ─── STEP 39: 모집 공고 등록 (관리자) ─────────────────────────────────
	@Test
	@Order(39)
	void step39_모집_공고_등록() throws Exception {
		RecruitmentRequest req = new RecruitmentRequest(
			"신입 모집",
			"자격요건 A, B",
			false,
			LocalDate.now().plusDays(1),
			LocalDate.now().plusDays(10)
		);

		String body = mockMvc.perform(post("/api/clubs/{clubId}/recruitments", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(req)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 공고 등록 완료"))
			.andExpect(jsonPath("$.data.id").isNumber())
			.andExpect(jsonPath("$.data.title").value("신입 모집"))
			.andExpect(jsonPath("$.data.requirements").value("자격요건 A, B"))
			.andReturn().getResponse().getContentAsString();

		// recruitmentId 추출
		recruitmentId = objectMapper.readTree(body).at("/data/id").asLong();
	}

	// ─── STEP 40: 모집 공고 단건 조회 ───────────────────────────────────
	@Test
	@Order(40)
	void step40_모집_공고_단건_조회() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/recruitment", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 공고 조회 성공"))
			.andExpect(jsonPath("$.data.id").value(recruitmentId))
			.andExpect(jsonPath("$.data.title").value("신입 모집"))
			.andExpect(jsonPath("$.data.requirements").value("자격요건 A, B"));
	}

	// ─── STEP 41: 전체 모집 공고 조회 ───────────────────────────────────
	@Test
	@Order(41)
	void step41_전체_모집_공고_조회() throws Exception {
		mockMvc.perform(get("/api/recruitments")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("전체 모집 공고 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			// id가 방금 등록한 recruitmentId를 포함하고 있는지 확인
			.andExpect(jsonPath(String.format("$.data[?(@.id==%d)]", recruitmentId)).exists());
	}

	// ─── STEP 42: OPEN 상태 모집 공고 조회 ────────────────────────────────
	@Test
	@Order(42)
	void step42_OPEN_모집_공고_조회() throws Exception {
		mockMvc.perform(get("/api/recruitments/open")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 중인 공고 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath(String.format("$.data[?(@.id==%d)]", recruitmentId)).exists());
	}

	// ─── STEP 43: CLOSED 상태 모집 공고 조회 (아직 없으므로 empty) ──────────
	@Test
	@Order(43)
	void step43_CLOSED_모집_공고_조회_비어있음() throws Exception {
		mockMvc.perform(get("/api/recruitments/closed")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 마감된 공고 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data").isEmpty());
	}

	// ─── STEP 44: 모집 공고 수정 (관리자) ───────────────────────────────────
	@Test
	@Order(44)
	void step44_모집_공고_수정() throws Exception {
		RecruitmentUpdateRequest upd = new RecruitmentUpdateRequest(
			"신입 모집(수정)",
			"자격요건 A, B, C",
			false,
			LocalDate.now().plusDays(2),
			LocalDate.now().plusDays(12)
		);
		mockMvc.perform(patch("/api/clubs/{clubId}/recruitment", clubId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(upd)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 공고 수정 완료"))
			.andExpect(jsonPath("$.data.title").value("신입 모집(수정)"))
			.andExpect(jsonPath("$.data.requirements").value("자격요건 A, B, C"))
			.andExpect(jsonPath("$.data.alwaysOpen").value(false));
	}

	// ─── STEP 45: 모집 공고 마감 (관리자) ───────────────────────────────────
	@Test
	@Order(45)
	void step45_모집_공고_마감() throws Exception {
		mockMvc.perform(patch("/api/clubs/{clubId}/recruitments/{recruitmentId}/close",
				clubId, recruitmentId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("모집 공고 마감 완료"));

		// 마감된 공고 조회 시 방금 닫은 recruitmentId가 목록에 나와야 한다
		mockMvc.perform(get("/api/recruitments/closed")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath(String.format("$.data[?(@.id==%d)]", recruitmentId)).exists());
	}

}
