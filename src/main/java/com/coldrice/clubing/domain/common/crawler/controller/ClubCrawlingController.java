package com.coldrice.clubing.domain.common.crawler.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.domain.common.crawler.service.ClubCrawlingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawler")
public class ClubCrawlingController {

	private final ClubCrawlingService clubCrawlingService;

	@PostMapping("/clubs")
	public String crawlClubData() {
		clubCrawlingService.crawlAndSaveClubs();
		return "클럽 최신화 완료";
	}
}
