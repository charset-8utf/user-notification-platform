package com.platform.commons.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
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
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ObservabilityFilterHandler chain = new ServerErrorMetricsHandler(
                exceptionMetrics,
                (req, res, servletChain) -> servletChain.doFilter(req, res));
        chain.handle(request, response, filterChain);
    }
}
