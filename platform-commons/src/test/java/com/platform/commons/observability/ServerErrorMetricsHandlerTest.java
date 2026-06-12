package com.platform.commons.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerErrorMetricsHandlerTest {

    @Test
    void recordsMetricForServerErrorStatus() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ExceptionMetrics metrics = new ExceptionMetrics(registry);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        when(response.getStatus()).thenReturn(503);

        ObservabilityFilterHandler chain = new ServerErrorMetricsHandler(
                metrics,
                (req, res, servletChain) -> servletChain.doFilter(req, res));
        chain.handle(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(registry.find("app.errors.total")
                .tag("source", "http")
                .tag("status", "503")
                .counter()).isNotNull();
    }

    @Test
    void skipsMetricForSuccessfulStatus() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ExceptionMetrics metrics = new ExceptionMetrics(registry);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        ObservabilityFilterHandler chain = new ServerErrorMetricsHandler(
                metrics,
                (req, res, servletChain) -> servletChain.doFilter(req, res));
        chain.handle(mock(HttpServletRequest.class), response, mock(FilterChain.class));

        assertThat(registry.find("app.errors.total").counters()).isEmpty();
    }
}
