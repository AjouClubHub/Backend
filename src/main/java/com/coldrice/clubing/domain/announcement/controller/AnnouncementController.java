package com.coldrice.clubing.domain.announcement.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.announcement.dto.AnnouncementRequest;
import com.coldrice.clubing.domain.announcement.dto.AnnouncementResponse;
import com.coldrice.clubing.domain.announcement.service.AnnouncementService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnnouncementController {

	private final AnnouncementService announcementService;

	@Secured("ROLE_MANAGER")
	@Operation(summary = "공지사항 등록", description = "클럽 관리자가 공지사항을 등록합니다.")
	@PostMapping("/api/clubs/{clubId}/announcements")
	public ResponseBodyDto<AnnouncementResponse> createAnnouncement(
		@PathVariable Long clubId,
		@RequestBody @Valid AnnouncementRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		AnnouncementResponse response = announcementService.createAnnouncement(clubId, request,
			userDetails.getMember());
		return ResponseBodyDto.success("공지사항 등록 완료", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "공지사항 수정", description = "공지사항 제목 및 내용을 수정합니다.")
	@PatchMapping("/api/clubs/{clubId}/announcements/{announcementId}")
	public ResponseBodyDto<AnnouncementResponse> updateAnnouncement(
		@PathVariable Long clubId,
		@PathVariable Long announcementId,
		@RequestBody AnnouncementRequest request, // @Valid 제거 : null 값 허용
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		AnnouncementResponse response = announcementService.updateAnnouncement(clubId, announcementId, request,
			userDetails.getMember());
		return ResponseBodyDto.success("공지사항 수정 완료", response);
	}

	@Operation(summary = "클럽 공지사항 목록 조회", description = "특정 클럽의 공지사항들을 모두 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/announcements")
	public ResponseBodyDto<List<AnnouncementResponse>> getClubAnnouncements(
		@PathVariable Long clubId
	) {
		List<AnnouncementResponse> response = announcementService.getAnnouncementsByClubId(clubId);
		return ResponseBodyDto.success("공지사항 목록 조회 성공", response);
	}

	@Operation(summary = "공지사항 단건 조회", description = "특정 공지사항을 단건 조회합니다. 조회수도 함께 증가합니다.")
	@GetMapping("/api/clubs/{clubId}/announcements/{announcementId}")
	public ResponseBodyDto<AnnouncementResponse> getAnnouncement(
		@PathVariable Long clubId,
		@PathVariable Long announcementId
	) {
		AnnouncementResponse response = announcementService.getAnnouncementById(clubId, announcementId);
		return ResponseBodyDto.success("공지사항 단건 조회 성공", response);
	}

}
