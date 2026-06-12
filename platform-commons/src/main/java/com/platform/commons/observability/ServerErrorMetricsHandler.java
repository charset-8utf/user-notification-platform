package com.platform.commons.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * После прохождения запроса записывает метрику HTTP 5xx.
 */
@RequiredArgsConstructor
public class ServerErrorMetricsHandler implements ObservabilityFilterHandler {

    private final ExceptionMetrics exceptionMetrics;
    private final ObservabilityFilterHandler next;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        next.handle(request, response, filterChain);
        int status = response.getStatus();
        if (status >= 500) {
            exceptionMetrics.recordHttpStatus(status);
        }
    }
}
