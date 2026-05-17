package com.crud.security;

import com.crud.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class JsonResponsesTest {

    @Test
    void ok_escapesStringFieldsInBody() {
        UserResponse raw = new UserResponse(1L, "<b>x</b>", "a@b.com", 20, LocalDateTime.now());

        ResponseEntity<UserResponse> response = JsonResponses.ok(raw);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.name()).isEqualTo("&lt;b&gt;x&lt;/b&gt;");
        assertThat(body.email()).isEqualTo("a@b.com");
    }

    @Test
    void created_escapesStringFieldsInBody() {
        UserResponse raw = new UserResponse(1L, "<script>", "u@test.com", 30, LocalDateTime.now());

        ResponseEntity<UserResponse> response = JsonResponses.created(raw);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.name()).doesNotContain("<script>");
    }
}
