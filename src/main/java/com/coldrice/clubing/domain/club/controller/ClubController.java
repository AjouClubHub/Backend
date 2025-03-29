package com.coldrice.clubing.domain.club.controller;

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
import com.coldrice.clubing.domain.club.dto.ClubApprovalRequest;
import com.coldrice.clubing.domain.club.dto.ClubRegisterRequest;
import com.coldrice.clubing.domain.club.dto.ClubRegisterResponse;
import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.club.service.ClubService;
import com.coldrice.clubing.util.ResponseBodyDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ClubController {

	private final ClubService clubService;

	@Secured("ROLE_MANAGER")
	@PostMapping("/api/clubs/register")
	public ResponseBodyDto<ClubRegisterResponse> registerClub(
		@RequestBody @Valid ClubRegisterRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ClubRegisterResponse response = clubService.register(request, userDetails.getMember());
		return ResponseBodyDto.success("클럽 등록 요청 완료", response);
	}

	@Secured("ROLE_ADMIN")
	@GetMapping("/api/admin/clubs")
	public ResponseBodyDto<List<ClubResponse>> getAllClubs() {
		List<ClubResponse> response = clubService.getAllClubs();
		return ResponseBodyDto.success("전체 클럽 목록 조회 성공", response);
	}

	@GetMapping("/api/clubs")
	public ResponseBodyDto<List<ClubResponse>> getApprovedClubs() {
		List<ClubResponse> response = clubService.getApprovedClubs();
		return ResponseBodyDto.success("승인된 클럽 목록 조회 성공", response);
	}

	// 클럽 등록 신청 승인/거절
	@Secured("ROLE_ADMIN")
	@PatchMapping("/api/admin/clubs/{clubId}/approval")
	public ResponseBodyDto<Void> updateClubApprovalStatus(
		@PathVariable Long clubId,
		@RequestBody ClubApprovalRequest request
	) {
		clubService.updateClubApproval(clubId, request);
		return ResponseBodyDto.success("클럽 상태 업데이트 성공");
	}
}
