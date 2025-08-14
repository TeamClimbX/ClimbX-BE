package com.climbx.climbx.common.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Amzn-Trace-Id";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String traceId = extractTraceId(request);

        MDC.put(TRACE_ID_MDC_KEY, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);

        if (traceId != null && !traceId.isEmpty()) {
            return parseAwsTraceId(traceId);
        }
        return generateTraceId();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /*
     * AWS의 X-Amzn-Trace-Id 헤더에서 Root 값을 추출.
     * X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
     */
    private String parseAwsTraceId(String awsTraceId) {
        String[] parts = awsTraceId.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("Root=")) {
                return trimmed.substring(5);
            }
        }
        return awsTraceId;
    }
}