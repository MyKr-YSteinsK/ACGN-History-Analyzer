package com.mykr.acgnhistoryanalyzer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Puts {@code requestId} into SLF4J {@link MDC} for the duration of the request (cleared in finally).
 */
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER_REQUEST_ID);
        String requestId = (incoming != null && !incoming.isBlank())
                ? incoming.trim()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        response.setHeader(HEADER_REQUEST_ID, requestId);
        MDC.put(MDC_REQUEST_ID, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID);
        }
    }
}
