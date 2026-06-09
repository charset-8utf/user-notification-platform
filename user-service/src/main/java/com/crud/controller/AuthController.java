package com.crud.controller;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.RefreshRequest;
import com.crud.dto.auth.TokenResponse;
import com.crud.security.AuthFacade;
import com.platform.commons.audit.AuditLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Profile("jwt")
@RequiredArgsConstructor
public class AuthController {

    private final AuthFacade authFacade;

    @AuditLog(action = "AUTH_LOGIN", resourceType = "auth")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authFacade.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authFacade.refresh(request);
    }

    @AuditLog(action = "AUTH_LOGOUT", resourceType = "auth")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authFacade.logout(request);
    }
}
