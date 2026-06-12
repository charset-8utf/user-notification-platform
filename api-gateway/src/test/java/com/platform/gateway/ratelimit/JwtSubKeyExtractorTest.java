package com.platform.gateway.ratelimit;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSubKeyExtractorTest {

    private final JwtSubKeyExtractor extractor = new JwtSubKeyExtractor();

    @Test
    void extractsSubjectFromBearerHeader() {
        String token = com.platform.gateway.support.GatewayJwtTestSupport.accessToken();

        Optional<String> key = extractor.subjectKeyFromBearer("Bearer " + token);

        assertThat(key).contains("sub:admin");
    }

    @Test
    void returnsEmptyForInvalidBearerHeader() {
        assertThat(extractor.subjectKeyFromBearer("Basic abc")).isEmpty();
        assertThat(extractor.subjectKeyFromBearer(null)).isEmpty();
    }
}
