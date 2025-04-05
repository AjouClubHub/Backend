package com.coldrice.clubing.domain.club.controller;

import java.util.List;

import org.apache.catalina.User;
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
import com.coldrice.clubing.domain.club.dto.ClubUpdateRequest;
import com.coldrice.clubing.domain.club.service.ClubService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ClubController {

	private final ClubService clubService;

	@Secured("ROLE_MANAGER")
	@Operation(summary = "클럽 등록 요청", description = "클럽 관리자가 새로운 클럽 등록을 요청합니다.")
	@PostMapping("/api/clubs/register")
	public ResponseBodyDto<ClubRegisterResponse> registerClub(
		@RequestBody @Valid ClubRegisterRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ClubRegisterResponse response = clubService.register(request, userDetails.getMember());
		return ResponseBodyDto.success("클럽 등록 요청 완료", response);
	}

	@Secured("ROLE_ADMIN")
	@Operation(summary = "전체 클럽 목록 조회 (관리자)", description = "관리자가 모든 클럽 목록을 status 기준 정렬로 조회합니다.")
	@GetMapping("/api/admin/clubs")
	public ResponseBodyDto<List<ClubResponse>> getAllClubs() {
		List<ClubResponse> response = clubService.getAllClubs();
		return ResponseBodyDto.success("전체 클럽 목록 조회 성공", response);
	}

	@Operation(summary = "승인된 클럽 목록 조회", description = "일반 사용자가 승인된 클럽 목록만 조회할 수 있습니다.")
	@GetMapping("/api/clubs")
	public ResponseBodyDto<List<ClubResponse>> getApprovedClubs() {
		List<ClubResponse> response = clubService.getApprovedClubs();
		return ResponseBodyDto.success("승인된 클럽 목록 조회 성공", response);
	}

	// 클럽 등록 신청 승인/거절
	@Secured("ROLE_ADMIN")
	@Operation(summary = "클럽 등록 승인/거절 처리", description = "관리자가 특정 클럽 등록 요청을 승인 또는 거절합니다.")
	@PatchMapping("/api/admin/clubs/{clubId}/approval")
	public ResponseBodyDto<Void> updateClubApprovalStatus(
		@Parameter(description = "승인/거절할 클럽 ID") @PathVariable Long clubId,
		@RequestBody ClubApprovalRequest request
	) {
		clubService.updateClubApproval(clubId, request);
		return ResponseBodyDto.success("클럽 상태 업데이트 성공");
	}

	@Operation(summary = "클럽 단건 조회", description = "클럽 ID로 단일 클럽 정보를 조회합니다.")
	@GetMapping("/api/clubs/{clubId}")
	public ResponseBodyDto<ClubResponse> getClub(
		@Parameter(description = "클럽 ID", example = "1") @PathVariable Long clubId
	) {
		ClubResponse response = clubService.getClubById(clubId);
		return ResponseBodyDto.success("클럽 단건 조회 성공", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "클럽 정보 수정", description = "클럽 소개, 연락처, 위치, 키워드를 수정합니다.")
	@PatchMapping("/api/clubs/{clubId}")
	public ResponseBodyDto<Void> updateClub(
		@PathVariable Long clubId,
		@RequestBody ClubUpdateRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		clubService.updateClub(clubId,request,userDetails.getMember());
		return ResponseBodyDto.success("클럽 정보 수정 완료");
	}
}
