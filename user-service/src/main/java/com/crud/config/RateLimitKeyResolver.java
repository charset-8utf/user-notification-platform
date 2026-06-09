package com.crud.config;

import com.crud.config.ratelimit.RateLimitKeyHandlerChain;
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
