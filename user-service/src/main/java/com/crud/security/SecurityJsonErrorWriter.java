package com.crud.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityJsonErrorWriter {

    private final JsonMapper jsonMapper;

    public void writeUnauthorized(HttpServletResponse response, String path) {
        write(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "Authentication required", path);
    }

    public void write(
            HttpServletResponse response,
            int status,
            String error,
            String message,
            String path
    ) {
        try {
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            Map<String, Object> body = Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", status,
                    "error", error,
                    "message", message,
                    "path", path);
            jsonMapper.writeValue(response.getOutputStream(), body);
        } catch (IOException ex) {
            log.error("Failed to write security error response for path={}", path, ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
