package com.coldrice.clubing.domain.announcement.entity;

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
public class Announcement extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Lob
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club club;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private Member createdBy;

	private int views;

	@Enumerated(EnumType.STRING)
	private AnnouncementCategory category;

	public void update(String title, String content) {
		if (title != null)
			this.title = title;
		if (content != null)
			this.content = content;
	}

	public void increaseView() {
		this.views++;
	}
}
