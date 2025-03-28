package com.coldrice.clubing.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/api/auth/signup")
	public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
		return authService.signup(signupRequest);
	}
}
