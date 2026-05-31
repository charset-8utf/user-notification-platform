package com.platform.bff.facade;

import com.platform.bff.dto.MeResponse;

public interface MeFacade {

    MeResponse loadCurrentUser(String authorizationHeader);
}
