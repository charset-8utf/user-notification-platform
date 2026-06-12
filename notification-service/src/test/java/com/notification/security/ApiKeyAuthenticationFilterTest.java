package com.notification.security;

import com.notification.config.NotificationApiProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApiKeyAuthenticationFilterTest {

    private static final String EMAIL_PATH = "/api/notifications/email";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validApiKeyAuthenticatesWriteRequest() throws Exception {
        ApiKeyProperties properties = new ApiKeyProperties();
        properties.setEnabled(true);
        properties.setKeys("demo-key");

        NotificationApiProperties notificationApiProperties = new NotificationApiProperties(EMAIL_PATH);

        ServiceJwtProperties serviceJwtProperties = new ServiceJwtProperties(
                "secret", "issuer", "audience", "subject", "notifications:write");
        ServiceJwtAuthorities authorities = new ServiceJwtAuthorities(serviceJwtProperties);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(
                properties, notificationApiProperties, authorities);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", EMAIL_PATH);
        request.addHeader(properties.getHeader(), "demo-key");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
        verify(chain).doFilter(request, response);
    }
}
