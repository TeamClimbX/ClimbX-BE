package com.climbx.climbx.common.middleware;

import com.climbx.climbx.common.util.TimeContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TimeTrackingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
        @NonNull
        HttpServletRequest request,

        @NonNull
        HttpServletResponse response,

        @NonNull
        Object handler
    ) {
        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();

        TimeContext.setStartTime(startTime);
        TimeContext.setPath(path);

        log.debug("Request started: {} {}", request.getMethod(), path);
        return true;
    }

    @Override
    public void afterCompletion(
        @NonNull
        HttpServletRequest request,

        @NonNull
        HttpServletResponse response,
        @NonNull
        Object handler,

        Exception ex
    ) {
        Long responseTime = TimeContext.getResponseTime();
        String path = TimeContext.getPath();

        if (responseTime != null) {
            log.debug("Request completed: {} {} - {}ms", request.getMethod(), path, responseTime);
        }

        TimeContext.clear();
    }
} 