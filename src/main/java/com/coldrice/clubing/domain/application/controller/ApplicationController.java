package com.coldrice.clubing.domain.application.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.announcement.dto.request;
import com.coldrice.clubing.domain.application.dto.ApplicationRequest;
import com.coldrice.clubing.domain.application.dto.ApplicationResponse;
import com.coldrice.clubing.domain.application.service.ApplicationService;
import com.coldrice.clubing.util.ResponseBodyDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

	private final ApplicationService applicationService;

	@PostMapping("/api/clubs/{clubId}/applications")
	public ResponseBodyDto<ApplicationResponse> applyToClub(
		@PathVariable Long clubId,
		@Valid @RequestBody ApplicationRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ApplicationResponse response = applicationService.apply(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("클럽 가입 신청 성공", response);
	}
}
