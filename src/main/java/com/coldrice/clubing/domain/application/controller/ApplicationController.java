package com.coldrice.clubing.domain.application.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.dto.ClubApplicationDecisonRequest;
import com.coldrice.clubing.domain.application.service.ApplicationService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

	private final ApplicationService applicationService;

	@Operation(summary = "클럽 가입 신청", description = "일반 사용자가 클럽 가입을 신청합니다.")
	@PostMapping("/api/clubs/{clubId}/applications")
	public ResponseBodyDto<ApplicationResponse> applyToClub(
		@Parameter(description = "가입 신청할 클럽의 ID") @PathVariable Long clubId,
		@Valid @RequestBody ApplicationRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ApplicationResponse response = applicationService.apply(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("클럽 가입 신청 성공", response);
	}

	// 클럽 가입 신청 목록 조회
	@Secured("ROLE_MANAGER")
	@Operation(summary = "클럽 가입 신청 목록 조회", description = "클럽 관리자가 자신의 클럽에 대한 가입 신청 목록을 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/applications")
	public ResponseBodyDto<List<ApplicationResponse>> getApplications(
		@Parameter(description = "가입 신청 목록을 조회할 클럽 ID") @PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<ApplicationResponse> response = applicationService.getAllApplications(clubId);
		return ResponseBodyDto.success("클럽 가입 신청 목록 조회 성공", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "가입 신청 승인/거절", description = "클럽 관리자가 특정 가입 신청을 승인 또는 거절합니다.")
	@PutMapping("/api/clubs/{clubId}/applications/{applicationId}/approval")
	public ResponseBodyDto<String> approveOrRejectApplication(
		@Parameter(description = "가입 신청이 들어온 클럽의 ID") @PathVariable Long clubId,
		@Parameter(description = "처리할 가입 신청 ID") @PathVariable Long applicationId,
		@RequestBody @Valid ClubApplicationDecisonRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		applicationService.decideApplication(clubId, applicationId, request, userDetails.getMember());
		return ResponseBodyDto.success("가입 승인/거절이 완료되었습니다");
	}
}
