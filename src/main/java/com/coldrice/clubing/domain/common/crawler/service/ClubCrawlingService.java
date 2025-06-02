package com.coldrice.clubing.domain.common.crawler.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.repository.ClubRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubCrawlingService {

	private final ClubRepository clubRepository;

	@Scheduled(cron = "0 0 0 */3 * *") // 매 3일마다 00시 00분에 실행
	@CacheEvict(value = "clubSearch", allEntries = true) // 캐시 삭제
	public void crawlAndSaveClubs() {
		Map<String, ClubCategory> categoryMap = Map.of(
			"club_list01.do", ClubCategory.과학기술분과,
			"club_list02.do", ClubCategory.레저스포츠분과,
			"club_list03.do", ClubCategory.사회활동분과,
			"club_list04.do", ClubCategory.연행예술분과,
			"club_list05.do", ClubCategory.종교분과,
			"club_list06.do", ClubCategory.창작전시분과,
			"club_list07.do", ClubCategory.체육분과,
			"club_list08.do", ClubCategory.학술언론분과,
			"club_list09.do", ClubCategory.준동아리
		);

		String baseListUrl = "https://www.ajou.ac.kr/kr/life/";
		List<String> failedUrls = new ArrayList<>();

		for (String fileName : categoryMap.keySet()) {
			ClubCategory category = categoryMap.get(fileName);

			try {
				Document listDoc = Jsoup.connect(baseListUrl + fileName)
					.timeout(5000)
					.get();

				Elements links = listDoc.select("div.link-box.d-ib > ul > li > a");

				for (Element link : links) {
					String detailUrl = link.absUrl("href");

					try {
						Connection.Response response = Jsoup.connect(detailUrl)
							.timeout(5000)
							.ignoreHttpErrors(true)
							.execute();

						if (response.statusCode() == 404) {
							System.out.println("❌ [404] 페이지 없음: " + detailUrl);
							failedUrls.add(detailUrl);
							continue;
						}

						Document detailDoc = response.parse();

						// 상세 페이지 파싱
						String name = detailDoc.selectFirst("div.support-box > p").text().trim();
						String description = detailDoc.select("h5.h5-tit01:contains(동아리 설명) + p").text().trim();

						Elements infoItems = detailDoc.select("ul.ul-type01 li");
						String location = cleanField(infoItems.get(0).text(), "위치 :");
						String contact = cleanField(infoItems.get(1).text(), "동아리 회장 연락처 :");

						String keyword = null;
						for (Element item : infoItems) {
							String text = item.text().trim();
							if (text.startsWith("#") || text.contains("#")) {
								keyword = text;
								break;
							}
						}
						final String finalKeyword = keyword;

						String sns = null;
						if (infoItems.size() >= 4) {
							Elements snsLinks = infoItems.get(3).select("a[href]");
							if (!snsLinks.isEmpty()) {
								sns = snsLinks.first().attr("href").trim();
							}
						}
						final String finalSns = sns;

						String style = detailDoc.select("div.support-wrap").attr("style");
						String imageUrl = null;
						Matcher matcher = Pattern.compile("url\\('([^']+)'\\)").matcher(style);
						if (matcher.find()) {
							String extracted = matcher.group(1);
							imageUrl = extracted.startsWith("http") ? extracted : "https://www.ajou.ac.kr" + extracted;
						}
						final String finalImageUrl = imageUrl;
						// 파싱 끝

						// ====== 별도 트랜잭션으로 저장/업데이트 ======
						saveOrUpdateSingleClub(
							name,
							description,
							category,
							contact,
							location,
							finalKeyword,
							finalSns,
							finalImageUrl
						);
						System.out.println("✅ 저장 또는 업데이트됨: " + name);

					} catch (Exception e) {
						System.out.println("⚠️ 상세페이지 파싱 실패: " + detailUrl);
						failedUrls.add(detailUrl);
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.out.println("⚠️ 목록 페이지 오류: " + fileName);
				e.printStackTrace();
			}
		}

		if (!failedUrls.isEmpty()) {
			System.out.println("\n📌 크롤링 실패한 상세페이지 목록:");
			failedUrls.forEach(url -> System.out.println("🔸 " + url));
		} else {
			System.out.println("\n🎉 모든 상세페이지 크롤링 성공!");
		}
	}

	/**
	 * 한 번 호출될 때마다 새로운 트랜잭션을 열어
	 * 단일 Club 엔티티 저장/업데이트를 수행합니다.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveOrUpdateSingleClub(
		String name,
		String description,
		ClubCategory category,
		String contact,
		String location,
		String keyword,
		String snsUrl,
		String imageUrl
	) {
		Club club = clubRepository.findByName(name)
			.map(existing -> {
				existing.updateClubInfo(
					description,
					category,
					contact,
					location,
					keyword,
					snsUrl,
					imageUrl
				);
				return existing;
			})
			.orElseGet(() -> Club.builder()
				.name(name)
				.description(description)
				.category(category)
				.contactInfo(contact)
				.location(location)
				.keyword(keyword)
				.snsUrl(snsUrl)
				.imageUrl(imageUrl)
				.type(ClubType.동아리)
				.status(ClubStatus.APPROVED)
				.build()
			);

		clubRepository.save(club);
	}

	private String cleanField(String text, String prefix) {
		return text.replace(prefix, "").trim();
	}
}
