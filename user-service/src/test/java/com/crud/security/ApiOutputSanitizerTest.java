package com.crud.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiOutputSanitizerTest {

    private final ApiOutputSanitizer sanitizer = new ApiOutputSanitizer();

    @Test
    void escapesHtmlInUserControlledText() {
        assertThat(sanitizer.sanitize("<script>alert(1)</script>"))
                .doesNotContain("<script>")
                .contains("script");
    }

    @Test
    void nullReturnsNull() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }
}
