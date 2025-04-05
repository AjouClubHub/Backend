package com.coldrice.clubing.domain.recruitment.controller;

import org.apache.catalina.User;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentRequest;
import com.coldrice.clubing.domain.recruitment.dto.RecruitmentResponse;
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
}
