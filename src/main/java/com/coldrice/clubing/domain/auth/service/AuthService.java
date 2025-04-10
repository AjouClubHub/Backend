package com.coldrice.clubing.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;
import com.coldrice.clubing.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	// private final S3Uploader s3Uploader

	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {

		// 아주대 이메일 도메인 확인
		if (!signupRequest.email().endsWith("@ajou.ac.kr")) {
			throw new GlobalException(ExceptionCode.INVALID_EMAIL_DOMAIN);
		}

		// 이메일 중복 확인
		if (memberRepository.existsByEmail(signupRequest.email())) {
			throw new GlobalException(ExceptionCode.EMAIL_DUPLICATE);
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.password());
		MemberRole memberRole = MemberRole.of(signupRequest.memberRole());

		Member newMember = Member.builder()
			.name(signupRequest.name())
			.email(signupRequest.email())
			.password(encodedPassword)
			.major(signupRequest.major())
			.memberRole(memberRole)
			.studentId(signupRequest.studentId())
			.build();

		Member savedMember = memberRepository.save(newMember);

		String barerToken = jwtUtil.createToken(
			savedMember.getId(),
			savedMember.getEmail(),
			memberRole);

		return new SignupResponse(barerToken);
	}

	public boolean isEmailDuplicate(String email) {
		return memberRepository.existsByEmail(email);
	}

	public boolean isStudentIdDuplicate(String studentId) {
		return memberRepository.existsByStudentId(studentId);
	}
}
