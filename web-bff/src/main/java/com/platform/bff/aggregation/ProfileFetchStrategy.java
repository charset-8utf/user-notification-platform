package com.platform.bff.aggregation;

import com.platform.bff.dto.ProfileSummary;
import org.jspecify.annotations.Nullable;

public interface ProfileFetchStrategy {

    ProfileSummary fetch(@Nullable Long userId, String authorizationHeader);
}
