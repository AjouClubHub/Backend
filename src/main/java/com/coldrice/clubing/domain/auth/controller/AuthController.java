package com.coldrice.clubing.domain.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.domain.auth.dto.EmailRequest;
import com.coldrice.clubing.domain.auth.dto.EmailVerificationRequest;
import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.auth.service.AuthService;
import com.coldrice.clubing.domain.common.email.EmailCodeManager;
import com.coldrice.clubing.domain.common.email.EmailService;
import com.coldrice.clubing.domain.common.email.EmailValidator;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;
	private final EmailService emailService;
	private final EmailCodeManager emailCodeManager;
	private final EmailValidator emailValidator;

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

	@Operation(summary = "이메일 인증코드 발송", description = "아주대 이메일로 인증코드를 전송합니다.")
	@PostMapping("/send-email")
	public ResponseBodyDto<Void> sendEmail(@RequestBody @Valid EmailRequest request) {
		emailValidator.validateAjouDomain(request.email());

		String code = emailService.sendAuthCode(request.email());
		emailCodeManager.saveAuthCode(request.email(), code);

		return ResponseBodyDto.success("이메일 인증코드 전송 완료");
	}

	@Operation(summary = "이메일 인증코드 검증", description = "입력한 인증코드가 이메일과 일치하는지 확인합니다.")
	@PostMapping("/verify-email")
	public ResponseBodyDto<Void> verifyEmail(
		@RequestBody EmailVerificationRequest request
	) {
		if (!emailCodeManager.verifyCode(request.email(), request.code())) {
			throw new GlobalException(ExceptionCode.INVALID_EMAIL_CODE);
		}

		// 인증 성공 → 인증된 이메일로 임시 캐시 저장
		return ResponseBodyDto.success("이메일 인증 성공");
	}

}
