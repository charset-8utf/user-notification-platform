package com.crud.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RateLimitKeyResolver {

    private final RateLimitKeyHandlerChain chain;

    public String resolve(HttpServletRequest request) {
        return chain.resolve(request);
    }
}
