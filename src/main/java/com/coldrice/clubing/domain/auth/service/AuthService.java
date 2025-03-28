package com.coldrice.clubing.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coldrice.clubing.domain.auth.dto.SignupRequest;
import com.coldrice.clubing.domain.auth.dto.SignupResponse;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.entity.MemberRole;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
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
		if (memberRepository.existsByEmail(signupRequest.email())) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}

		String encodedPassword = passwordEncoder.encode(signupRequest.password());

		MemberRole memberRole = MemberRole.of(signupRequest.memberRole());

		Member newMember = Member.builder()
			.name(signupRequest.name())
			.email(signupRequest.email())
			.password(encodedPassword)
			.department(signupRequest.department())
			.memberRole(memberRole)
			.build();

		Member savedMember = memberRepository.save(newMember);

		String barerToken = jwtUtil.createToken(
			savedMember.getId(),
			savedMember.getEmail(),
			memberRole);

		return new SignupResponse(barerToken);
	}
}
