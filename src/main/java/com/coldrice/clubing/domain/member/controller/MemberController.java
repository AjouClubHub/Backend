package com.coldrice.clubing.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.member.dto.MyPageResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.service.MemberService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
	private final MemberService memberService;

	@Operation(summary = "마이페이지 조회", description = "회원의 마이페이지 정보를 조회합니다. (회원 정보, 가입 클럽, 가입 신청 현황, 알림 목록 포함)")
	@GetMapping("/mypage")
	public ResponseBodyDto<MyPageResponse> getMyPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		Member member = userDetails.getMember();
		MyPageResponse response = memberService.getMyPage(member);
		return ResponseBodyDto.success("마이페이지 조회 성공", response);
	}
}
