package com.coldrice.clubing.domain.club.service;

import org.springframework.stereotype.Service;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.repository.ClubRepository;
import com.coldrice.clubing.domain.common.sms.SmsService;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.member.repository.MemberRepository;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClubManagerAuthService {
	private final ClubRepository clubRepository;
	private final MemberRepository memberRepository;
	private final SmsService smsService;

	public void requestVerification(Long clubId, String phoneNumber) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		if (!club.getContactInfo().equals(phoneNumber)) {
			throw new GlobalException(ExceptionCode.PHONE_MISMATCH);
		}

		smsService.sendVerificationCode(phoneNumber);
	}

	public void verifyCodeAndPromoteManager(Long clubId, String phoneNumber, String code, Member member) {
		Club club = clubRepository.findById(clubId)
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_CLUB));

		if (!club.getContactInfo().equals(phoneNumber)) {
			throw new GlobalException(ExceptionCode.PHONE_MISMATCH);
		}

		if (!smsService.verifyCode(phoneNumber, code)) {
			throw new GlobalException(ExceptionCode.INVALID_CODE);
		}

		// 클럽의 매니저로 등록
		member.updateRoleToManager();
		memberRepository.save(member);

		club.updateManager(member);
		clubRepository.save(club);
	}
}
