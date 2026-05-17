package com.crud.security;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.RefreshRequest;
import com.crud.security.jwt.InvalidRefreshTokenException;
import com.crud.security.jwt.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    private final UserDetails user = User.builder()
            .username("admin")
            .password("hash")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .build();

    @Test
    void login_validCredentials_returnsTokens() {
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtTokenService.issueTokenPair(user))
                .thenReturn(new JwtTokenService.TokenPair("access", "refresh", 900));

        var response = authService.login(new LoginRequest("admin", "secret"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        LoginRequest request = new LoginRequest("admin", "wrong");
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_validToken_returnsNewPair() {
        when(jwtTokenService.rotateRefreshToken("rt-1"))
                .thenReturn(new JwtTokenService.TokenPair("access2", "refresh2", 900));

        var response = authService.refresh(new RefreshRequest("rt-1"));

        assertThat(response.accessToken()).isEqualTo("access2");
    }

    @Test
    void refresh_invalidToken_throwsBadCredentials() {
        when(jwtTokenService.rotateRefreshToken("bad"))
                .thenThrow(new InvalidRefreshTokenException("invalid"));

        RefreshRequest request = new RefreshRequest("bad");
        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void logout_revokesRefreshToken() {
        authService.logout(new RefreshRequest("rt-1"));
        verify(jwtTokenService).revokeRefreshToken("rt-1");
    }
}
