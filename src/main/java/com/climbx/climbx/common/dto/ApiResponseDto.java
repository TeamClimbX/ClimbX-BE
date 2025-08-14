package com.climbx.climbx.common.dto;

import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.util.TimeContext;
import java.time.Instant;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ApiResponseDto<T>(

    Long httpStatus,
    String statusMessage,
    Instant timeStamp,
    Long responseTimeMs,
    String path,
    T data
) {

    public static <T> ApiResponseDto<T> success(T data) {
        /*
         * success
         */
        return ApiResponseDto.<T>builder()
            .httpStatus((long) HttpStatus.OK.value())
            .statusMessage("SUCCESS")
            .timeStamp(Instant.now())
            .responseTimeMs(TimeContext.getResponseTime())
            .path(TimeContext.getPath())
            .data(data)
            .build();
    }

    public static <T> ApiResponseDto<T> success(T data, HttpStatus httpStatus) {
        /*
         * success
         */
        return ApiResponseDto.<T>builder()
            .httpStatus((long) httpStatus.value())
            .statusMessage("SUCCESS")
            .timeStamp(Instant.now())
            .responseTimeMs(TimeContext.getResponseTime())
            .path(TimeContext.getPath())
            .data(data)
            .build();
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        /*
         * success with custom message
         */
        return ApiResponseDto.<T>builder()
            .httpStatus((long) HttpStatus.OK.value())
            .statusMessage(message)
            .timeStamp(Instant.now())
            .responseTimeMs(TimeContext.getResponseTime())
            .path(TimeContext.getPath())
            .data(data)
            .build();
    }

    public static <T> ApiResponseDto<T> error(ErrorCode errorCode) {
        return ApiResponseDto.<T>builder()
            .httpStatus((long) errorCode.status().value())
            .statusMessage(errorCode.message())
            .timeStamp(Instant.now())
            .responseTimeMs(TimeContext.getResponseTime())
            .path(TimeContext.getPath())
            .data(null)
            .build();
    }

    public static <T> ApiResponseDto<T> error(HttpStatus httpStatus, String message) {
        /*
         * error
         */
        return ApiResponseDto.<T>builder()
            .httpStatus((long) httpStatus.value())
            .statusMessage(message)
            .timeStamp(Instant.now())
            .responseTimeMs(TimeContext.getResponseTime())
            .path(TimeContext.getPath())
            .data(null)
            .build();
    }

}
