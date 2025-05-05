package com.coldrice.clubing.domain.application.dto;

import com.coldrice.clubing.domain.application.entity.Application;

public record ApplicationDetailWrapper(
	ApplicationResponse application,
	ApplicationInfoResponse applicationInfo
) {
	public static ApplicationDetailWrapper from(Application application) {
		return new ApplicationDetailWrapper(
			ApplicationResponse.from(application),
			ApplicationInfoResponse.from(application)
		);
	}
}
