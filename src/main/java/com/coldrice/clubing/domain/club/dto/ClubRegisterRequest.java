package com.coldrice.clubing.domain.club.dto;

import java.util.List;

import com.coldrice.clubing.domain.club.entity.ClubCategory;
import com.coldrice.clubing.domain.club.entity.ClubType;
import com.coldrice.clubing.domain.club.entity.RequiredMajor;

public record ClubRegisterRequest(
	String name,
	String description,
	ClubType type, // 동아리 또는 소학회
	ClubCategory category,
	String contactInfo,
	String location,
	String keyword,
	List<RequiredMajor> requiredMajors
) {
}
