package com.coldrice.clubing.domain.membership.dto;

import jakarta.validation.constraints.NotBlank;

public record ClubMemberExpelRequest(
	@NotBlank(message = "추방 사유는 필수입니다.")
	String reason
) {
}
