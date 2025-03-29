package com.coldrice.clubing.domain.club.dto;

import com.coldrice.clubing.domain.club.entity.ClubStatus;

public record ClubRegisterResponse(
	Long clubId,
	String name,
	String status // open, closed, pending
) {
}
