package com.coldrice.clubing.domain.announcement.dto;

import com.coldrice.clubing.domain.announcement.entity.AnnouncementCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "공지사항 등록 및 수정 요청")
public record AnnouncementRequest(
	@Schema(description = "공지 제목", example = "2025년 1학기 OT 공지")
	@NotBlank(message = "제목은 필수입니다.")
	String title,

	@Schema(description = "공지 내용", example = "OT는 3월 4일에 진행됩니다.")
	@NotBlank(message = "내용은 필수입니다.")
	String content,

	@Schema(description = "공지 카테고리", example = "모임, 스터디, 기타")
	@NotBlank(message = "카테고리 선택은 필수입니다.")
	AnnouncementCategory category
	) {
}
