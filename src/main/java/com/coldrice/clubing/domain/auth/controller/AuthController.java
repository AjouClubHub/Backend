package com.coldrice.clubing.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.auth.service.AuthService;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	private final MemberRepository memberRepository;

	@PostMapping("/signup")
	public ResponseBodyDto<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
		SignupResponse response = authService.signup(signupRequest);
		return ResponseBodyDto.success("회원가입 성공", response);
	}

	@Operation(summary = "이메일 중복 체크", description = "이메일이 이미 존재하는지 확인합니다.")
	@GetMapping("/email")
	public ResponseBodyDto<Boolean> checkEmail(@RequestParam String email) {
		boolean isDuplicate = authService.isEmailDuplicate(email);
		return ResponseBodyDto.success("이메일 중복 여부 조회 완료", isDuplicate);
	}

	@Operation(summary = "학번 중복 체크", description = "학번이 이미 존재하는지 확인합니다.")
	@GetMapping("/studentId")
	public ResponseBodyDto<Boolean> checkStudentId(@RequestParam String studentId) {
		boolean isDuplicate = authService.isStudentIdDuplicate(studentId);
		return ResponseBodyDto.success("학번 중복 여부 확인", isDuplicate);
	}
}
