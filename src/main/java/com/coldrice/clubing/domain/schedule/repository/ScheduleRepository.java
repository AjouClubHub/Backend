package com.coldrice.clubing.domain.schedule.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coldrice.clubing.domain.schedule.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
	List<Schedule> findByClubIdAndStartTimeBetween(Long clubId, LocalDateTime start, LocalDateTime end);
}
