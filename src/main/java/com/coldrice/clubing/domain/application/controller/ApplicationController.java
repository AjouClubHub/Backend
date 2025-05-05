package com.coldrice.clubing.domain.application.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.application.dto.ApplicationDetailWrapper;
import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.dto.ClubApplicationDecisonRequest;
import com.coldrice.clubing.domain.application.dto.RejectionReasonResponse;
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
	@Operation(summary = "클럽 가입 신청 단건 조회", description = "클럽 관리자가 자신의 클럽에 대한 가입 신청을 단건 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/applications/{applicationId}")
	public ResponseBodyDto<ApplicationDetailWrapper> getApplication(
		@PathVariable Long clubId,
		@PathVariable Long applicationId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ApplicationDetailWrapper response = applicationService.getAllApplicationDetail(clubId, applicationId, userDetails.getMember());
		return ResponseBodyDto.success("클럽 가입 신청 단건 조회 성공", response);
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

	@Operation(summary = "내 클럽 가입 신청 현황 조회", description = "사용자가 본인이 신청한 클럽 가입 상태를 확인합니다,")
	@GetMapping("/api/my/applications")
	public ResponseBodyDto<List<ApplicationResponse>> getMyApplication(
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<ApplicationResponse> response = applicationService.getMyApplications(userDetails.getMember());
		return ResponseBodyDto.success("가입 신청 현황 조회 성공", response);
	}

	@Operation(summary = "클럽 가입 거절 사유 조회", description = "거절된 클럽 신청의 사유를 확인합니다.")
	@GetMapping("/api/my/applications/{applicationId}/rejection")
	public ResponseBodyDto<RejectionReasonResponse> getRejectionReason(
		@PathVariable Long applicationId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		RejectionReasonResponse response = applicationService.getRejectionReason(applicationId, userDetails.getMember().getId());
		return ResponseBodyDto.success("거절 사유 조회 성공", response);
	}

	@Operation(summary = "클럽 가입 신청 취소", description = "사용자가 가입 신청(PENDING 상태)을 취소합니다.")
	@DeleteMapping("/api/clubs/{clubId}/applications")
	public ResponseBodyDto<Void> cancelApplication(
		@PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		applicationService.cancelApplication(clubId, userDetails.getMember());
		return ResponseBodyDto.success("가입 신청이 취소되었습니다.");
	}


}
