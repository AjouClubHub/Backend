package com.coldrice.clubing.domain.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.member.entity.Member;
import com.coldrice.clubing.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(Member receiver);

	List<Notification> findByReceiverOrderByCreatedAtDesc(Member receiver);
}
