package com.coldrice.clubing.domain.membership.controller;

import java.util.List;

import org.apache.catalina.User;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.club.dto.ClubResponse;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.membership.dto.ClubMemberResponse;
import com.coldrice.clubing.domain.membership.dto.ClubWithdrawRequest;
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

	@Secured("ROLE_MEMBER")
	@Operation(summary = "클럽 탈퇴", description = "사용자가 자신이 가입한 클럽에서 탈퇴합니다.")
	@PostMapping("/api/clubs/{clubId}/withdraw")
	public ResponseBodyDto<Void> withdrawClub(
		@PathVariable Long clubId,
		@RequestBody ClubWithdrawRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		membershipService.withdrawClub(clubId, userDetails.getMember(), request.leavenReason());
		return ResponseBodyDto.success("클럽 탈퇴가 완료되었습니다.");
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "클럽 회원 목록 조회", description = "클럽 관리자가 자신의 클럽 회원 목록을 조회합니다.(ACTIVE 상태의 회원만)")
	@GetMapping("/api/clubs/{clubId}/members")
	public ResponseBodyDto<List<ClubMemberResponse>> getClubMembers(
		@PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<ClubMemberResponse> response = membershipService.getClubMembers(clubId,userDetails.getMember());
		return ResponseBodyDto.success("클럽 회원 목록 조회 성공", response);
	}
}
