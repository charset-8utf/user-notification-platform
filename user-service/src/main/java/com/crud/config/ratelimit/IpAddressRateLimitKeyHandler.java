package com.crud.config.ratelimit;

import java.util.Optional;

final class IpAddressRateLimitKeyHandler extends AbstractRateLimitKeyHandler {

    IpAddressRateLimitKeyHandler() {
        super(null);
    }

    @Override
    protected Optional<String> doHandle(RateLimitKeyContext context) {
        return Optional.ofNullable(context.request().getRemoteAddr())
                .filter(address -> !address.isBlank())
                .map(address -> "ip:" + address);
    }
}
