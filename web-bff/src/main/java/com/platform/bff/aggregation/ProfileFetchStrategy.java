package com.platform.bff.aggregation;

import com.platform.bff.dto.ProfileSummary;

public interface ProfileFetchStrategy {

    ProfileSummary fetch(Long userId, String authorizationHeader);
}
