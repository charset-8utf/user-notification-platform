package com.crud.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyHandlerChain {

    private final RateLimitKeyHandler head;

    public RateLimitKeyHandlerChain(
            @Qualifier(RateLimitConfig.RATE_LIMIT_KEY_HANDLER_HEAD) RateLimitKeyHandler head) {
        this.head = head;
    }

    public String resolve(HttpServletRequest request) {
        return head.handle(new RateLimitKeyContext(request)).orElse("ip:unknown");
    }
}
