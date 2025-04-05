package com.coldrice.clubing.domain.club.dto;

public record ClubUpdateRequest(
	String description,
	String contactInfo,
	String location,
	String keyword
) {
}
