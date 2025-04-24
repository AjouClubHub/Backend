package com.coldrice.clubing.domain.club.entity;

import java.util.ArrayList;
import java.util.List;

import com.coldrice.clubing.domain.common.Timestamped;
import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.exception.customException.GlobalException;
import com.coldrice.clubing.exception.enums.ExceptionCode;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
public class Club extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Enumerated(EnumType.STRING)
	private ClubType type;

	private String description;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private ClubStatus status = ClubStatus.PENDING;

	@Enumerated(EnumType.STRING)
	private ClubCategory category;

	private String contactInfo; // 전화번호

	@ElementCollection(fetch = FetchType.LAZY)
	@Enumerated(EnumType.STRING)
	@CollectionTable(name = "club_requirement_majors", joinColumns = @JoinColumn(name = "club_id"))
	@Column(name = "requirement_major")
	private List<RequiredMajor> requiredMajors = new ArrayList<>();

	private String location;

	private String keyword;

	// 등록 요청 거절 사유 (관리자가 요청 거절시)
	// 또는 ENUM으로 거절 사유 항목을 설정 할 수도 있음.
	private String rejectionReason;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "manager_id")
	private Member manager; // member : club  == 1:N

	private String imageUrl;

	private String snsUrl;

	public void updateStatus(ClubStatus status, String rejectionReason) {
		this.status = status;
		this.rejectionReason = rejectionReason;
	}

	public void validateManager(Member member) {
		if (!this.manager.getId().equals(member.getId())) {
			throw new GlobalException(ExceptionCode.UNAUTHORIZED_MANAGER);
		}
	}

	public void updateInfo(String description, String contactInfo, String location, String keyword) {
		if (description != null)
			this.description = description;
		if (contactInfo != null)
			this.contactInfo = contactInfo;
		if (location != null)
			this.location = location;
		if (keyword != null)
			this.keyword = keyword;
	}

	public void updateClubInfo(String description,
		ClubCategory category,
		String contactInfo,
		String location,
		String keyword,
		String snsUrl,
		String imageUrl) {
		this.description = description;
		this.category = category;
		this.contactInfo = contactInfo;
		this.location = location;
		this.keyword = keyword;
		this.snsUrl = snsUrl;
		this.imageUrl = imageUrl;
	}

	public void updateManager(Member member) {
		this.manager = member;
		this.status = ClubStatus.APPROVED; // 인증 성공 시 승인 상태로 전환
	}
}
