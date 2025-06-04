package com.coldrice.clubing.domain.club.entity;

import java.util.Arrays;

public enum RequiredMajor {
	기계공학과, 산업공학과, 화학공학과, 첨단신소재공학과, 응용화학생명공학과, 응용화학과, 환경안전공학과, 건설시스템공학과,
	교통시스템공학과, 건축학과, 융합시스템공학과, 전자공학과, 미래모빌리티공학과, 지능형반도체공학과, 디지털미디어학과,
	국방디지털융합학과, 인공지능융합학과, 소프트웨어학과, 사이버보안학과, 수학과, 물리학과, 프런티어과학학부, 화학과,
	생명과학과, 경영학과, 경영인텔리전스학과, 금융공학과, 글로벌경영학과, 국어국문학과, 영어영문학과, 불어불문학과, 사학과,
	문화콘텐츠학과, 경제학과, 행정학과, 심리학과, 경제정치사회융합학부, 사회학과, 정치외교학과, 스포츠레저학과, 의학과,
	간호학과, 약학과, 첨단바이오융합대학, 다산학부대학, 자유전공학부, 국제학부;

	public static RequiredMajor of(String role) {
		return Arrays.stream(RequiredMajor.values())
			.filter(r -> r.name().equalsIgnoreCase(role))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 MemberRole"));
	}
}
