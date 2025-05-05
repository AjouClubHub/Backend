package com.coldrice.clubing.domain.schedule.entity;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.common.Timestamped;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class Schedule extends Timestamped {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Club club;

	private String title;
	private String content;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
}
