package com.platform.commons.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@ConditionalOnWebApplication
@ConditionalOnClass(name = "jakarta.servlet.Filter")
@ConditionalOnBean(ExceptionMetrics.class)
@RequiredArgsConstructor
public class HttpErrorMetricsFilter extends OncePerRequestFilter {

    private final ExceptionMetrics exceptionMetrics;

    @Override
    @NullMarked
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ObservabilityFilterHandler chain = (req, res, chainFilter) -> {
            chainFilter.doFilter(req, res);
            recordServerError(res);
        };
        chain.handle(request, response, filterChain);
    }

    private void recordServerError(HttpServletResponse response) {
        int status = response.getStatus();
        if (status >= 500) {
            exceptionMetrics.recordHttpStatus(status);
        }
    }
}
