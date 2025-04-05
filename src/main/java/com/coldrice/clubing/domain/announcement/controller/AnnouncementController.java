package com.coldrice.clubing.domain.announcement.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}
