package com.coldrice.clubing.domain.membership.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.membership.repository.MembershipRepository;
import com.coldrice.clubing.domain.membership.service.MembershipService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MembershipController {

	private final MembershipService membershipService;

	@Secured("ROLE_MEMBER")
	@Operation(summary = "내가 가입한 클럽 목록 조회", description = "로그인한 사용자가 현재 가입 중인 클럽 목록을 조회합니다.")
	@GetMapping("/api/my/clubs")
	public ResponseBodyDto<List<MyClubResponse>> getMyClubs(
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<MyClubResponse> response = membershipService.getMyClubs(userDetails.getMember());
		return ResponseBodyDto.success("내 가입된 클럽 목록 조회 성공", response);
	}
}
