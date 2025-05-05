package com.coldrice.clubing.domain.membership.dto;

import com.coldrice.clubing.domain.application.dto.ApplicationInfoResponse;
import com.coldrice.clubing.domain.application.entity.Application;
import com.coldrice.clubing.domain.membership.entity.Membership;

public record ClubMemberDetailWrapper(
	ClubMemberResponse member,
	ApplicationInfoResponse applicationInfo
) {
	public static ClubMemberDetailWrapper of(Membership membership, Application application) {
		return new ClubMemberDetailWrapper(
			ClubMemberResponse.from(membership),
			ApplicationInfoResponse.from(application)
		);
	}
}
