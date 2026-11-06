package com.festa.user.api.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Incrementa festa_user_requests_total{action,outcome}. action = método + ruta
 * normalizada (se reemplazan segmentos UUID por {id}). outcome = status code.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MetricsFilter extends OncePerRequestFilter {

    private static final String METRIC = "festa_user_requests_total";
    private static final String UUID_SEGMENT = "[0-9a-fA-F\\-]{36}";

    private final MeterRegistry registry;

    public MetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            String action = request.getMethod() + " " + normalize(request.getRequestURI());
            String outcome = String.valueOf(response.getStatus());
            Counter.builder(METRIC)
                    .tag("action", action)
                    .tag("outcome", outcome)
                    .register(registry)
                    .increment();
        }
    }

    private String normalize(String uri) {
        return uri.replaceAll(UUID_SEGMENT, "{id}");
    }
}