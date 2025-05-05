package com.coldrice.clubing.domain.schedule.contorller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.coldrice.clubing.config.security.UserDetailsImpl;
import com.coldrice.clubing.domain.schedule.dto.ScheduleRequest;
import com.coldrice.clubing.domain.schedule.dto.ScheduleResponse;
import com.coldrice.clubing.domain.schedule.service.ScheduleService;
import com.coldrice.clubing.util.ResponseBodyDto;

import io.swagger.v3.oas.annotations.Operation;
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
}
