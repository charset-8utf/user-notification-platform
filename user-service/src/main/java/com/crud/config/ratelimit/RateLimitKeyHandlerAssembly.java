package com.crud.config.ratelimit;

import org.springframework.stereotype.Component;

@Component
class RateLimitKeyHandlerAssembly {

    RateLimitKeyHandler head() {
        RateLimitKeyHandler ip = new IpAddressRateLimitKeyHandler();
        RateLimitKeyHandler basic = new BasicAuthRateLimitKeyHandler(ip);
        RateLimitKeyHandler bearer = new BearerJwtRateLimitKeyHandler(basic);
        return new SecurityContextRateLimitKeyHandler(bearer);
    }
}
