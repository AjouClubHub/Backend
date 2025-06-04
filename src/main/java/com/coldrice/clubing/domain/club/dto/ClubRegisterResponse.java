package com.coldrice.clubing.domain.club.dto;

public record ClubRegisterResponse(
	Long clubId,
	String name,
	String status // open, closed, pending
) {
}
