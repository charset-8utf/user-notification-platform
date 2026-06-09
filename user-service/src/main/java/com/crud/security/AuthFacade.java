package com.crud.security;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.RefreshRequest;
import com.crud.dto.auth.TokenResponse;

public interface AuthFacade {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    void logout(RefreshRequest request);
}
