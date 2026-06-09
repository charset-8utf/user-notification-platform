package com.crud.security;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.RefreshRequest;
import com.crud.dto.auth.TokenResponse;
import com.crud.repository.CredentialRepository;
import com.crud.security.jwt.InvalidRefreshTokenException;
import com.crud.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("jwt")
@RequiredArgsConstructor
public class AuthService implements AuthFacade {

    private final UserDetailsService userDetailsService;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        UserDetails user = userDetailsService.loadUserByUsername(request.username());
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Неверное имя пользователя или пароль");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Учётная запись отключена");
        }
        String email = credentialRepository.findByUsername(request.username())
                .map(credential -> credential.getUser().getEmail())
                .orElse(null);
        return TokenResponse.from(jwtTokenService.issueTokenPair(user, email));
    }

    public TokenResponse refresh(RefreshRequest request) {
        try {
            return TokenResponse.from(jwtTokenService.rotateRefreshToken(request.refreshToken()));
        } catch (InvalidRefreshTokenException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        }
    }

    public void logout(RefreshRequest request) {
        jwtTokenService.revokeRefreshToken(request.refreshToken());
    }
}
