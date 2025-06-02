package com.coldrice.clubing.integration;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class ClubCrawlingTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ClubRepository clubRepository;

	@BeforeEach
	void setup() {
		// 테스트 시작 전에 DB 비우기
		clubRepository.deleteAll();
	}

	@Test
	void crawlClubsEndpoint_shouldSaveSomeClubsAndReturnSuccessMessage() {
		// 1) HTTP POST 요청 보내기
		String url = "http://localhost:" + port + "/api/crawler/clubs";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

		// 2) 응답 상태 코드 및 본문 메시지 검증
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo("클럽 최신화 완료");

		// 3) H2 DB에 실제로 Club 엔티티가 저장되었는지 확인
		List<Club> savedClubs = clubRepository.findAll();
		assertThat(savedClubs)
			.as("크롤링 결과로 최소 하나 이상의 Club 레코드가 저장되어야 한다")
			.isNotEmpty();

		// 4) 저장된 첫 번째 엔티티 필드 검증 (예시)
		Club anyClub = savedClubs.get(0);
		assertThat(anyClub.getName()).isNotBlank();
		assertThat(anyClub.getCategory()).isNotNull();
		assertThat(anyClub.getContactInfo()).isNotNull();
	}
}
