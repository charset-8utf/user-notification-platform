package com.crud.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

public record RateLimitKeyContext(HttpServletRequest request) {
}
