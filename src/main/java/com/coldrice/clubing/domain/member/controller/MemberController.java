package com.coldrice.clubing.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.member.dto.MyPageResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.service.MemberService;
import com.coldrice.clubing.util.ResponseBodyDto;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
	private final MemberService memberService;

	public ResponseBodyDto<MyPageResponse> getMyPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		Member member = userDetails.getMember();
		MyPageResponse response = memberService.getMyPage(member);
		return ResponseBodyDto.success("마이페이지 조회 성공", response);
	}
}
