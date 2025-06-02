package com.coldrice.clubing.integration;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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

import com.coldrice.clubing.domain.announcement.dto.AnnouncementRequest;
import com.coldrice.clubing.domain.announcement.entity.Announcement;
import com.coldrice.clubing.domain.announcement.entity.AnnouncementCategory;
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
	@Autowired
	private AnnouncementRepository announcementRepository;

	static Member user;
	static Member manager;
	static String userToken;
	static String managerToken;

	static Long clubId;
	static Long applicationId;
	static Long announcementId;

	@BeforeAll
	static void setupShared(
		@Autowired MemberRepository memberRepository,
		@Autowired ClubRepository clubRepository,
		@Autowired JwtUtil jwtUtil,
		@Autowired AnnouncementRepository announcementRepository
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

		Announcement ann = Announcement.builder()
			.title("Original Title")
			.content("Original Content")
			.category(AnnouncementCategory.모임)
			.club(club)
			.createdBy(manager)
			.build();
		announcementId = announcementRepository.save(ann).getId();
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

	// @Test
	// @Order(1)
	// void step1_클럽_관리자_인증코드_요청() throws Exception {
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
	@Order(2)
	void step2_클럽_관리자_인증_성공() throws Exception {

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
	@Order(3)
	void step3_승인된_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value("승인된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@Order(4)
	void step4_클럽_카테고리_검색_성공() throws Exception {
		mockMvc.perform(get("/api/clubs/search")
				.param("category", "운동")) // 실제 저장된 카테고리와 맞게 수정
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 검색 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@Order(5)
	void step5_클럽_가입_신청_일반회원() throws Exception {

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

	@Test
	@Order(6)
	void step6_내_클럽_가입_신청_현황_조회() throws Exception {
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

	@Test
	@Order(7)
	void step7_가입_신청_취소_및_재신청() throws Exception {
		// 1) 가입 신청 취소
		mockMvc.perform(delete("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 신청이 취소되었습니다."));

		// DB에서 해당 application이 삭제되었는지 확인
		assertThat(applicationRepository.findByClubIdAndMemberId(clubId, user.getId())).isEmpty();

		// 2) 동일 내용으로 재신청
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

		// 새로 저장된 applicationId 업데이트
		applicationId = applicationRepository.findByClubIdAndMemberId(clubId, user.getId())
			.orElseThrow()
			.getId();
	}


	@Test
	@Order(8)
	void step8_가입_신청_목록_조회_관리자() throws Exception {

		// when & then: 관리자(권한: MANAGER)가 해당 클럽의 신청 목록 조회
		mockMvc.perform(get("/api/clubs/{clubId}/applications", clubId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	@Order(9)
	void step9_가입_신청_단건_조회_관리자() throws Exception {
		mockMvc.perform(get("/api/clubs/{clubId}/applications/{applicationId}", clubId, applicationId)
				.header("Authorization", managerToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 가입 신청 단건 조회 성공"))
			// application 객체 내의 필드를 확인
			.andExpect(jsonPath("$.data.application.applicationId").value(applicationId))
			.andExpect(jsonPath("$.data.application.clubType").value("동아리"))
			.andExpect(jsonPath("$.data.application.clubName").value("SMS 인증 테스트 클럽"))
			.andExpect(jsonPath("$.data.application.memberName").value("일반 사용자"))
			.andExpect(jsonPath("$.data.application.status").value("PENDING"))
			// applicationInfo 객체 내의 필드를 확인 (매핑 순서에 따라 key-값 위치가 달라지므로 주의)
			.andExpect(jsonPath("$.data.applicationInfo.birthDate").value("2000-05-24"))
			.andExpect(jsonPath("$.data.applicationInfo.studentId").value("여자"))
			.andExpect(jsonPath("$.data.applicationInfo.gender").value("010-1234-5678"))
			.andExpect(jsonPath("$.data.applicationInfo.phoneNumber").value("열심히 활동하고 싶습니다!"))
			.andExpect(jsonPath("$.data.applicationInfo.motivation").value(user.getStudentId()));
	}

	@Test
	@Order(10)
	void step10_가입_신청_단건_조회_권한없음() throws Exception {
		// userToken(ROLE_MEMBER)으로 단건 조회 시도 → 403 Forbidden
		mockMvc.perform(get("/api/clubs/{clubId}/applications/{applicationId}", clubId, applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isForbidden());
	}


	@Test
	@Order(11)
	void step11_가입_거절_관리자() throws Exception {
		// given
		String requestBody = """
			    {
			        "status": "REJECTED",
			        "rejectionReason": "정원이 초과되었습니다"
			    }
			""";

		// when & then
		mockMvc.perform(put("/api/clubs/{clubId}/applications/{applicationId}/approval", clubId, applicationId)
				.header("Authorization", managerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("가입 승인/거절이 완료되었습니다"));
	}

	@Test
	@Order(12)
	void step12_가입_거절_알림_확인_일반회원() throws Exception {
		mockMvc.perform(get("/api/notifications")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("알림 전체 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].content").value("SMS 인증 테스트 클럽에서 가입이 거절되었습니다.")); // 알림 메시지 예시
	}

	// TODO: 거절 사유 확인
	@Test
	@Order(13)
	void step13_거절_사유_정상_조회() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("거절 사유 조회 성공"))
			.andExpect(jsonPath("$.data.rejectionReason").value("정원이 초과되었습니다")); // 실제 사유에 맞게 수정
	}

	@Test
	@Order(14)
	void step14_다른_사용자가_거절_사유_조회시_예외() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", managerToken)) // 다른 사람의 신청 건 조회
			.andExpect(status().isUnauthorized()); // 또는 isUnauthorized() depending on exception handling
	}

	@Test
	@Order(15)
	void step15_가입_승인_관리자() throws Exception {
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
	@Order(16)
	void step12_거절되지_않은_신청_사유_조회시_예외() throws Exception {
		mockMvc.perform(get("/api/my/applications/{applicationId}/rejection", applicationId)
				.header("Authorization", userToken))
			.andExpect(status().isBadRequest()); // APPLICATION_NOT_REJECTED
	}

	// TODO 일반 사용자는 승인 알림 확인

	@Test
	@Order(17)
	void step17_공지사항_등록_관리자() throws Exception {
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
	@Order(18)
	void step18_일정_등록_관리자() throws Exception {
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
	@Order(19)
	void step19_내가_가입한_클럽_목록_조회_일반회원() throws Exception {
		mockMvc.perform(get("/api/my/clubs")
				.header("Authorization", userToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("내 가입된 클럽 목록 조회 성공"))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0].clubId").value(clubId));
	}

	@Test
	@Order(20)
	void step20_공지사항_조회_일반회원() throws Exception {
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
	@Order(21)
	void step21_일정_조회_일반회원() throws Exception {
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

	@Test
	@Order(22)
	void step22_클럽_탈퇴_일반회원() throws Exception {
		// given: 사용자가 이미 가입된 클럽이 있다고 가정(step10에서 승인되어 membership 생성됨)

		String requestBody = """
			{
			    "leaveReason": "개인 사정"
			}
			""";

		// when & then: DELETE 요청으로 클럽 탈퇴
		mockMvc.perform(delete("/api/clubs/{clubId}/withdraw", clubId)
				.header("Authorization", userToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("클럽 탈퇴가 완료되었습니다."));
	}

	@Test
	@Order(23)
	void step23_공지사항_수정_관리자() throws Exception {
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

		// DB 확인
		Announcement updated = announcementRepository.findById(announcementId).orElseThrow();
		assertThat(updated.getTitle()).isEqualTo("Updated Title");
		assertThat(updated.getContent()).isEqualTo("Updated Content");
	}

	@Test
	@Order(24)
	void step24_공지사항_수정_권한없음() throws Exception {
		// 일반 회원(user)로 수정 시도
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

	@Test
	@Order(25)
	void step25_공지사항_수정_잘못된_클럽_또는_공지() throws Exception {
		// 존재하지 않는 clubId 조합
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

	@Test
	@Order(26)
	void step26_공지사항_단건_조회_조회수_증가() throws Exception {
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

}
