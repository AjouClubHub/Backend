package com.coldrice.clubing.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.auth.service.AuthService;
import com.coldrice.clubing.util.ResponseBodyDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/api/auth/signup")
	public ResponseBodyDto<SignupResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
		SignupResponse response = authService.signup(signupRequest);
		return ResponseBodyDto.success("회원가입 성공", response);
	}
}
