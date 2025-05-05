package com.coldrice.clubing.domain.schedule.contorller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.schedule.dto.ScheduleRequest;
import com.coldrice.clubing.domain.schedule.dto.ScheduleResponse;
import com.coldrice.clubing.domain.schedule.service.ScheduleService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	@Secured("ROLE_MANAGER")
	@Operation(summary = "일정 등록", description = "클럽 관리자가 일정을 등록합니다.")
	@PostMapping("/api/clubs/{clubId}/schedules")
	public ResponseBodyDto<ScheduleResponse> createSchedule(
		@PathVariable Long clubId,
		@RequestBody ScheduleRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ScheduleResponse response = scheduleService.createSchedule(clubId, request, userDetails.getMember());
		return ResponseBodyDto.success("일정 등록 완료", response);
	}

	@Operation(summary = "클럽 일정 목록 조회", description = "기간(start~end) 동안의 클럽 일정을 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/schedules")
	public ResponseBodyDto<List<ScheduleResponse>> getSchedules(
		@PathVariable Long clubId,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
	) {
		List<ScheduleResponse> response = scheduleService.getSchedulesByPeriod(clubId, start, end);
		return ResponseBodyDto.success("일정 조회 성공", response);
	}

	@Operation(summary = "클럽 일정 단건 조회", description = "클럽 일정 상세 정보를 조회합니다.")
	@GetMapping("/api/clubs/{clubId}/schedules/{scheduleId}")
	public ResponseBodyDto<ScheduleResponse> getSchedule(
		@PathVariable Long clubId,
		@PathVariable Long scheduleId
	) {
		ScheduleResponse response = scheduleService.getSchedule(clubId, scheduleId);
		return ResponseBodyDto.success("일정 조회 성공", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "일정 수정", description = "클럽 관리자가 일정을 수정합니다.")
	@PutMapping("/api/clubs/{clubId}/schedules/{scheduleId}")
	public ResponseBodyDto<ScheduleResponse> updateSchedule(
		@PathVariable Long clubId,
		@PathVariable Long scheduleId,
		@RequestBody @Valid ScheduleRequest request,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		ScheduleResponse response = scheduleService.updateSchedule(clubId, scheduleId, request, userDetails.getMember());
		return ResponseBodyDto.success("일정 수정 완료", response);
	}

	@Secured("ROLE_MANAGER")
	@Operation(summary = "일정 삭제", description = "클럽 관리자가 일정을 삭제합니다.")
	@DeleteMapping("/api/clubs/{clubId}/schedules/{scheduleId}")
	public ResponseBodyDto<String> deleteSchedule(
		@PathVariable Long clubId,
		@PathVariable Long scheduleId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		scheduleService.deleteSchedule(clubId, scheduleId, userDetails.getMember());
		return ResponseBodyDto.success("일정 삭제 완료");
	}

}
