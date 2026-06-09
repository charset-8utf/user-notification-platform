package com.crud.integration;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.RefreshRequest;
import com.crud.dto.auth.TokenResponse;
import com.crud.support.JwtAuthTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginRefreshLogoutFlow() {
        HttpHeaders json = new HttpHeaders();
        json.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<TokenResponse> login = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/login",
                new HttpEntity<>(new LoginRequest("admin", "admin123"), json),
                TokenResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse loginBody = login.getBody();
        assertThat(loginBody).isNotNull();
        assertThat(loginBody.accessToken()).isNotBlank();
        assertThat(loginBody.refreshToken()).isNotBlank();

        ResponseEntity<TokenResponse> refresh = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/refresh",
                new HttpEntity<>(new RefreshRequest(loginBody.refreshToken()), json),
                TokenResponse.class);
        assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.OK);
        TokenResponse refreshBody = refresh.getBody();
        assertThat(refreshBody).isNotNull();
        assertThat(refreshBody.refreshToken()).isNotEqualTo(loginBody.refreshToken());
        assertThat(refreshBody.accessToken()).isNotBlank();

        HttpHeaders bearer = JwtAuthTestSupport.bearerHeaders(refreshBody.accessToken());
        ResponseEntity<String> users = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.GET, new HttpEntity<>(bearer), String.class);
        assertThat(users.getStatusCode()).isEqualTo(HttpStatus.OK);

        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/logout",
                new HttpEntity<>(new RefreshRequest(refreshBody.refreshToken()), bearer),
                Void.class);

        ResponseEntity<TokenResponse> refreshAgain = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/refresh",
                new HttpEntity<>(new RefreshRequest(refreshBody.refreshToken()), json),
                TokenResponse.class);
        assertThat(refreshAgain.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apiWithoutTokenReturns401() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/users", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
