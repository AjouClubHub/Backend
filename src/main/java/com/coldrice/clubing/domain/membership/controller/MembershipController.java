package com.coldrice.clubing.domain.membership.controller;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.membership.dto.ClubMemberApplicationResponse;
import com.coldrice.clubing.domain.membership.dto.ClubMemberExpelRequest;
import com.coldrice.clubing.domain.membership.dto.ClubMemberResponse;
import com.coldrice.clubing.domain.membership.dto.ClubWithdrawRequest;
import com.coldrice.clubing.domain.membership.dto.ManagedClubResponse;
import com.coldrice.clubing.domain.membership.dto.MyClubResponse;
import com.coldrice.clubing.domain.membership.service.MembershipService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MembershipController {

	private final MembershipService membershipService;

	@Operation(summary = "내가 가입한 클럽 목록 조회", description = "로그인한 사용자가 현재 가입 중인 클럽 목록을 조회합니다.")
	@GetMapping("/api/my/clubs")
	public ResponseBodyDto<List<MyClubResponse>> getMyClubs(
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<MyClubResponse> response = membershipService.getMyClubs(userDetails.getMember());
		return ResponseBodyDto.success("내 가입된 클럽 목록 조회 성공", response);
	}

	@Operation(summary = "내가 가입한 클럽 단건 조회", description = "가입중인 클럽")
	@GetMapping("/api/my/clubs/{clubId}")
	public ResponseBodyDto<MyClubResponse> getMyClubById(
		@PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		MyClubResponse response = membershipService.getMyClubById(clubId, userDetails.getMember());
		return ResponseBodyDto.success("내 가입된 클럽 상세 조회 성공", response);
	}

	@Operation(summary = "클럽 탈퇴", description = "로그인한 사용자가 가입한 클럽 중 하나를 상세 조회합니다.")
	@DeleteMapping("/api/clubs/{clubId}/withdraw")
	public ResponseBodyDto<Void> withdrawClub(
		@PathVariable Long clubId,
		@RequestBody ClubWithdrawRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		membershipService.withdrawClub(clubId, userDetails.getMember(), request.leavenReason());
		return ResponseBodyDto.success("클럽 탈퇴가 완료되었습니다.");
	}

	@Operation(summary = "클럽 회원 목록 조회", description = "자신의 클럽 회원 목록을 조회합니다.(ACTIVE 상태의 회원만)")
	@GetMapping("/api/clubs/{clubId}/members")
	public ResponseBodyDto<List<ClubMemberResponse>> getClubMembers(
		@PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<ClubMemberResponse> response = membershipService.getClubMembers(clubId, userDetails.getMember());
		return ResponseBodyDto.success("클럽 회원 목록 조회 성공", response);
	}

	@Operation(summary = "클럽 회원의 가입 신청 정보 단건 조회", description = "클럽 관리자가 자신의 클럽 회원의 가입 신청 정보를 단건 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/members/{memberId}")
	public ResponseBodyDto<ClubMemberApplicationResponse> getClubMember(
		@PathVariable Long clubId,
		@PathVariable Long memberId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ClubMemberApplicationResponse response = membershipService.getClubMember(clubId, memberId, userDetails.getMember());
		return ResponseBodyDto.success("클럽 회원 단건 조회 성공", response);
	}


	@Secured("ROLE_MANAGER")
	@Operation(summary = "회원 추방", description = "클럽 관리자가 클럽 회원을 추방합니다.")
	@DeleteMapping("/api/clubs/{clubId}/members/{memberId}/expel")
	public ResponseBodyDto<Void> expelMember(
		@PathVariable Long clubId,
		@PathVariable Long memberId,
		@RequestBody ClubMemberExpelRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		membershipService.expelClubMember(clubId, memberId, userDetails.getMember(), request.reason());
		return ResponseBodyDto.success("회원 추방 완료");
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "내가 관리하는 클럽 목록 조회", description = "클럽 관리자 권한을 가진 사용자가 관리중인 클럽 목록을 조회합니다.")
	@GetMapping("/api/my/manage-clubs")
	public ResponseBodyDto<List<ManagedClubResponse>> getMyManagedClubs(
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		List<ManagedClubResponse> response = membershipService.getMyManagedClubs(userDetails.getMember());
		return ResponseBodyDto.success("관리중인 클럽 목록 조회 성공", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "관리 중인 클럽 단건 조회", description = "클럽 관리자 권한을 가진 사용자가 관리 중인 특정 클럽의 상세 정보를 조회합니다.")
	@GetMapping("/api/my/manage-clubs/{clubId}")
	public ResponseBodyDto<ManagedClubResponse> getMyManagedClubById(
		@PathVariable Long clubId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ManagedClubResponse response = membershipService.getMyManagedClubById(clubId, userDetails.getMember());
		return ResponseBodyDto.success("관리중인 클럽 상세 조회 성공", response);
	}
}
