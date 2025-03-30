package com.coldrice.clubing.domain.application.entity;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.common.Timestamped;
import com.coldrice.clubing.domain.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Application extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club club;

	@Enumerated(EnumType.STRING)
	private ApplicationStatus status;

	private String rejectionReason;

	private String birthDate;

	private String studentId;

	private String major;

	private String gender;

	private String phoneNumber;

	private String motivation;

	public void reject(String reason) {
		this.status = ApplicationStatus.REJECTED;
		this.rejectionReason = reason;
	}

	public void approve() {
		this.status = ApplicationStatus.APPROVED;
		this.rejectionReason = null; // 기존 거절 사유가 있을 수 있으므로 초기화
	}
}
