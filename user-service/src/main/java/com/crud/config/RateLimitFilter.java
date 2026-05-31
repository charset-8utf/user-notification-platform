package com.crud.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
class RateLimitFilter extends OncePerRequestFilter {

    private final JsonMapper jsonMapper;
    private final RateLimitKeyResolver rateLimitKeyResolver;
    private final int maxRequests;
    private final long windowSeconds;
    private final Cache<String, RequestWindow> windows;

    RateLimitFilter(
            JsonMapper jsonMapper,
            RateLimitKeyResolver rateLimitKeyResolver,
            int maxRequests,
            long windowSeconds
    ) {
        this.jsonMapper = jsonMapper;
        this.rateLimitKeyResolver = rateLimitKeyResolver;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.windows = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(windowSeconds))
                .maximumSize(10_000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = rateLimitKeyResolver.resolve(request);
        RequestWindow window = windows.asMap().compute(
                key,
                (k, existing) ->
                        existing == null || existing.isExpired(windowSeconds) ? new RequestWindow() : existing
        );

        if (window.increment() > maxRequests) {
            log.warn("Rate limit exceeded for {} on {} {}", key, request.getMethod(), request.getRequestURI());
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            Map<String, Object> error = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 429,
                    "error", "Too Many Requests",
                    "message", "Rate limit exceeded. Maximum " + maxRequests + " requests per " + windowSeconds + " seconds.",
                    "path", request.getRequestURI()
            );

            jsonMapper.writeValue(response.getOutputStream(), error);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static final class RequestWindow {
        private final Instant windowStart = Instant.now();
        private final AtomicInteger count = new AtomicInteger(0);

        boolean isExpired(long configuredWindowSeconds) {
            return Instant.now().isAfter(windowStart.plusSeconds(configuredWindowSeconds));
        }

        int increment() {
            return count.incrementAndGet();
        }
    }
}
