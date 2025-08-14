package com.climbx.climbx.common.util;

public class TimeContext {

    private static final ThreadLocal<Long> startTimeHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> pathHolder = new ThreadLocal<>();

    public static void setStartTime(long startTime) {
        startTimeHolder.set(startTime);
    }

    public static String getPath() {
        return pathHolder.get();
    }

    public static void setPath(String path) {
        pathHolder.set(path);
    }

    public static Long getResponseTime() {
        Long startTime = startTimeHolder.get();
        return startTime != null ? System.currentTimeMillis() - startTime : null;
    }

    public static void clear() {
        startTimeHolder.remove();
        pathHolder.remove();
    }
} 