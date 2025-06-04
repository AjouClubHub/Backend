package com.coldrice.clubing.domain.recruitment.controller;

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
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentRequest;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentResponse;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentUpdateRequest;
import com.coldrice.clubing.domain.recruitment.entity.RecruitmentStatus;
import com.coldrice.clubing.domain.recruitment.service.RecruitmentService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

	private final RecruitmentService recruitmentService;

	@Secured("ROLE_MANAGER")
	@Operation(summary = "모집 공고 등록", description = "클럽 관리자가 해당 클럽의 모집 공고를 등록합니다.")
	@PostMapping("/api/clubs/{clubId}/recruitments")
	public ResponseBodyDto<RecruitmentResponse> registerRecruitment(
		@PathVariable Long clubId,
		@RequestBody @Valid RecruitmentRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		RecruitmentResponse response = recruitmentService.registerRecruitment(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("모집 공고 등록 완료", response);
	}

	@Operation(summary = "모집 공고 단건 조회", description = "클럽의 모집 공고를 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/recruitment")
	public ResponseBodyDto<RecruitmentResponse> getRecruitment(@PathVariable Long clubId) {
		RecruitmentResponse response = recruitmentService.getRecruitment(clubId);
		return ResponseBodyDto.success("모집 공고 조회 성공", response);
	}

	@Operation(summary = "전체 모집 공고 조회", description = "현재 등록된 모든 모집 공고를 조회합니다.")
	@GetMapping("/api/recruitments")
	public ResponseBodyDto<List<RecruitmentResponse>> getAllRecruitments() {
		List<RecruitmentResponse> response = recruitmentService.getAllRecruitments();
		return ResponseBodyDto.success("전체 모집 공고 조회 성공", response);
	}

	@Operation(summary = "모집 중인 공고 조회", description = "현재 모집 중(OPEN)인 모집 공고를 조회합니다.")
	@GetMapping("/api/recruitments/open")
	public ResponseBodyDto<List<RecruitmentResponse>> getOpenRecruitments() {
		List<RecruitmentResponse> response = recruitmentService.getRecruitmentsByStatus(RecruitmentStatus.OPEN);
		return ResponseBodyDto.success("모집 중인 공고 조회 성공", response);
	}

	@Operation(summary = "모집 완료된 공고 조회", description = "모집 마감(CLOSED)된 모집 공고를 조회합니다.")
	@GetMapping("/api/recruitments/closed")
	public ResponseBodyDto<List<RecruitmentResponse>> getClosedRecruitments() {
		List<RecruitmentResponse> response = recruitmentService.getRecruitmentsByStatus(RecruitmentStatus.CLOSED);
		return ResponseBodyDto.success("모집 마감된 공고 조회 성공", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "모집 공고 수정", description = "클럽 관리자가 모집 공고 내용을 수정합니다.")
	@PatchMapping("/api/clubs/{clubId}/recruitment")
	public ResponseBodyDto<RecruitmentResponse> updateRecruitment(
		@PathVariable Long clubId,
		@Valid @RequestBody RecruitmentUpdateRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		RecruitmentResponse response = recruitmentService.updateRecruitment(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("모집 공고 수정 완료", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "모집 공고 마감", description = "클럽 관리자가 특정 모집 공고를 마감합니다.")
	@PatchMapping("/api/clubs/{clubId}/recruitments/{recruitmentId}/close")
	public ResponseBodyDto<Void> closeRecruitment(
		@PathVariable Long clubId,
		@PathVariable Long recruitmentId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		recruitmentService.closeRecruitment(clubId, recruitmentId, userDetails.getMember());
		return ResponseBodyDto.success("모집 공고 마감 완료");
	}

}
