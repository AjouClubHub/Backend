package com.coldrice.clubing.domain.club.dto;

public record ClubSearchRequest(
	String name,
	String category,
	String keyword
) {
}
