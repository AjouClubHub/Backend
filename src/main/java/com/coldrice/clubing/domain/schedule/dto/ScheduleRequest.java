package com.coldrice.clubing.domain.schedule.dto;

import java.time.LocalDateTime;

public record ScheduleRequest(
	String title,
	String content,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
}
