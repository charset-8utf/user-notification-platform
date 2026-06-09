package com.crud.support;

import com.crud.dto.auth.LoginRequest;
import com.crud.dto.auth.TokenResponse;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class JwtAuthTestSupport {

    private JwtAuthTestSupport() {
    }

    public static String obtainAccessToken(RestTemplate restTemplate, int port, String username, String password) {
        String url = "http://localhost:" + port + "/api/auth/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(new LoginRequest(username, password), headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(url, entity, TokenResponse.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Login failed: " + response.getStatusCode());
        }
        return response.getBody().accessToken();
    }

    public static HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
