package com.crud.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiOutputSanitizerTest {

    @Test
    void escapesHtmlInUserControlledText() {
        assertThat(ApiOutputSanitizer.sanitize("<script>alert(1)</script>"))
                .doesNotContain("<script>")
                .contains("script");
    }

    @Test
    void nullReturnsNull() {
        assertThat(ApiOutputSanitizer.sanitize(null)).isNull();
    }
}
