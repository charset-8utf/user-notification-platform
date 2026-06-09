package com.platform.commons.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@FunctionalInterface
public interface ObservabilityFilterHandler {

    void handle(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException;
}
