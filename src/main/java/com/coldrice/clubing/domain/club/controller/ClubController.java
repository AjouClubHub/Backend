package com.coldrice.clubing.domain.club.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.coldrice.clubing.domain.club.dto.ClubSearchRequest;
import com.coldrice.clubing.domain.club.dto.ClubUpdateRequest;
import com.coldrice.clubing.domain.club.service.ClubManagerAuthService;
import com.coldrice.clubing.domain.club.service.ClubService;
import com.coldrice.clubing.domain.common.sms.dto.SmsRequest;
import com.coldrice.clubing.domain.common.sms.dto.SmsVerificationRequest;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ClubController {

	private final ClubService clubService;
	private final ClubManagerAuthService clubManagerAuthService;

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

	@Operation(summary = "PENDING 상태의 클럽 목록 조회", description = "사용자가 관리자가 등록되지 않은 PENDING 상태의 클럽 목록을 조회합니다.")
	@GetMapping("/api/clubs/pending")
	public ResponseBodyDto<List<ClubResponse>> getPendingClubs() {
		List<ClubResponse> response = clubService.getPendingClubs();
		return ResponseBodyDto.success("관리자 미인증 클럽 목록 조회 성공", response);
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
		clubService.updateClub(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("클럽 정보 수정 완료");
	}

	@Operation(summary = "클럽 관리자 인증번호 요청",
		description = "해당 클럽의 등록된 전화번호로 인증 코드를 전송합니다. 클럽의 contactInfo와 일치해야 합니다.")
	@PostMapping("/api/clubs/{clubId}/manager-auth/request")
	public ResponseBodyDto<Void> requestManagerAuth(
		@PathVariable Long clubId,
		@RequestBody SmsRequest request
	) {
		clubManagerAuthService.requestVerification(clubId, request.phoneNumber());
		return ResponseBodyDto.success("인증 코드 전송 완료");
	}

	@Operation(summary = "클럽 관리자 인증 코드 검증",
		description = "사용자가 받은 인증 코드를 검증하고, 검증에 성공하면 사용자 권한을 MANAGER로 변경하고 클럽의 관리자 정보를 등록합니다.")
	@PatchMapping("/api/clubs/{clubId}/manager-auth/verify")
	public ResponseBodyDto<Void> verifyManagerAuthCode(
		@PathVariable Long clubId,
		@RequestBody @Valid SmsVerificationRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		clubManagerAuthService.verifyCodeAndPromoteManager(clubId, request.phoneNumber(), request.code(),
			userDetails.getMember());
		return ResponseBodyDto.success("클럽 관리자 인증 완료");
	}

	@Operation(summary = "클럽 검색", description = "카테고리, 키워드, 이름 등 조겅을 통해 클럽을 검색합니다.")
	@GetMapping("/api/clubs/search")
	public ResponseBodyDto<List<ClubResponse>> searchClubs(
		@ModelAttribute ClubSearchRequest request
	) {
		List<ClubResponse> response = clubService.searchClubs(request);
		return ResponseBodyDto.success("클럽 검색 성공", response);
	}

}
