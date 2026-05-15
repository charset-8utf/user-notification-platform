package com.crud.config;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class SanitizationConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OncePerRequestFilter xssSanitizationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                filterChain.doFilter(new SanitizedRequest(request), response);
            }
        };
    }

    private static class SanitizedRequest extends HttpServletRequestWrapper {

        SanitizedRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public @Nullable String getParameter(String name) {
            return Optional.ofNullable(super.getParameter(name))
                    .map(HtmlUtils::htmlEscape)
                    .orElse(null);
        }

        @Override
        public String @Nullable [] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            return values != null ? sanitize(values) : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return super.getParameterMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> sanitize(entry.getValue())
                    ));
        }

        private static String[] sanitize(String[] values) {
            return Arrays.stream(values)
                    .map(HtmlUtils::htmlEscape)
                    .toArray(String[]::new);
        }
    }
}
