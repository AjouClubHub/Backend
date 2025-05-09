package com.coldrice.clubing.domain.recruitment.entity;

import java.time.LocalDate;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.common.Timestamped;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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
public class Recruitment extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club club;

	private LocalDate startDate;

	private LocalDate endDate;

	@Lob
	private String requirements;

	@Enumerated(EnumType.STRING)
	private RecruitmentStatus status;

	private String title;

	private boolean alwaysOpen;

	public void update(String title, String requirements, LocalDate startDate, LocalDate endDate) {
		this.title = title;
		this.requirements = requirements;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void close() {
		this.status = RecruitmentStatus.CLOSED;
	}
}
