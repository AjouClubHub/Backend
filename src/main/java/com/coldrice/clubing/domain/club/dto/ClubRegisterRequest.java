package com.coldrice.clubing.domain.club.dto;

import java.util.List;

import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.entity.RequiredMajor;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클럽 등록 요청")
public record ClubRegisterRequest(
	@Schema(description = "클럽명", example = "아주 개발 동아리")
	String name,

	@Schema(description = "클럽 설명", example = "개발 공부와 프로젝트를 진행하는 동아리입니다.")
	String description,

	@Schema(description = "클럽 타입", example = "동아리 or 소학회")
	ClubType type,

	@Schema(description = "클럽 카테고리", example = "IT")
	ClubCategory category,

	@Schema(description = "연락처", example = "010-1234-5678")
	String contactInfo,

	@Schema(description = "활동 위치", example = "팔달관 B101")
	String location,

	@Schema(description = "키워드", example = "개발, 프로그래밍, 스터디")
	String keyword,

	@Schema(description = "지원 가능한 전공 목록")
	List<RequiredMajor> requiredMajors
) {
}
