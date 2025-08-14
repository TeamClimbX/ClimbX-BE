package com.climbx.climbx.common.middleware;

import com.climbx.climbx.common.annotation.SuccessStatus;
import com.climbx.climbx.common.dto.ApiResponseDto;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
        @NonNull MethodParameter returnType,
        @NonNull Class<? extends HttpMessageConverter<?>> _converterType
    ) {
        return !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(
        Object body,
        @NonNull MethodParameter returnType,
        @NonNull MediaType _selectedContentType,
        @NonNull Class<? extends HttpMessageConverter<?>> _selectedConverterType,
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response
    ) {
        // Swagger 관련 경로는 ApiResponse로 래핑하지 않음
        String path = request.getURI().getPath();
        if (isSwaggerPath(path)) {
            return body;
        }

        // ApiResponse<?> 타입이면, HttpStatusCode를 설정하고 그대로 돌려보내기
        if (body instanceof ApiResponseDto<?>) {
            response.setStatusCode(
                HttpStatusCode.valueOf(((ApiResponseDto<?>) body).httpStatus().intValue())
            );
            return body;
        }

        // @SuccessStatus 어노테이션이 있는지 확인
        SuccessStatus successStatus = returnType.getMethodAnnotation(SuccessStatus.class);
        HttpStatus httpStatus = successStatus != null ? successStatus.value() : HttpStatus.OK;

        response.setStatusCode(HttpStatusCode.valueOf(httpStatus.value()));
        // 그 외의 경우 ApiResponse로 래핑
        return ApiResponseDto.success(body, httpStatus);
    }

    private boolean isSwaggerPath(String path) {
        return path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs");
    }
}