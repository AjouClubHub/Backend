package com.coldrice.clubing.domain.notification.entity;

import com.coldrice.clubing.domain.common.Timestamped;
import com.coldrice.clubing.domain.member.entity.Member;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Notification extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Member receiver;

	private String content;

	private boolean isRead = false;

	@Lob
	@Enumerated(EnumType.STRING)
	private NotificationType type;

	public static Notification from(Member receiver, String content, NotificationType type) {
		return Notification.builder()
			.receiver(receiver)
			.content(content)
			.isRead(false)
			.type(type)
			.build();
	}

	public void markAsRead() {
		this.isRead = true;
	}
}
