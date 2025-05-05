package com.coldrice.clubing.domain.schedule.dto;

import java.time.LocalDateTime;

import com.coldrice.clubing.domain.schedule.entity.Schedule;

public record ScheduleResponse(
	Long id,
	String title,
	String content,
	LocalDateTime startTime,
	LocalDateTime endTime
) {
	public static ScheduleResponse from(Schedule schedule) {
		return new ScheduleResponse(
			schedule.getId(),
			schedule.getTitle(),
			schedule.getContent(),
			schedule.getStartTime(),
			schedule.getEndTime()
		);
	}
}
