package com.crud.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitKeyHandlerChain {

    private final RateLimitKeyHandler head;

    public RateLimitKeyHandlerChain() {
        this(buildDefaultChain());
    }

    private RateLimitKeyHandlerChain(RateLimitKeyHandler head) {
        this.head = head;
    }

    public static RateLimitKeyHandlerChain forTests() {
        return new RateLimitKeyHandlerChain(buildDefaultChain());
    }

    public String resolve(HttpServletRequest request) {
        return head.handle(new RateLimitKeyContext(request)).orElse("ip:unknown");
    }

    public static RateLimitKeyHandler buildDefaultChain() {
        RateLimitKeyHandler ip = new IpAddressRateLimitKeyHandler();
        RateLimitKeyHandler basic = new BasicAuthRateLimitKeyHandler(ip);
        RateLimitKeyHandler bearer = new BearerJwtRateLimitKeyHandler(basic);
        return new SecurityContextRateLimitKeyHandler(bearer);
    }
}
