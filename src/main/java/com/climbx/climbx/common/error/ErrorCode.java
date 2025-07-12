package com.climbx.climbx.common.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum ErrorCode {

    // 4xx : Client Errors
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "유효성 검사에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 통계 정보를 찾을 수 없습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청 허용 횟수를 초과했습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    GYM_NOT_FOUND(HttpStatus.NOT_FOUND, "클라이밍장을 찾을 수 없습니다."),
    NICKNAME_MISMATCH(HttpStatus.BAD_REQUEST, "닉네임이 일치하지 않습니다."),

    // Auth Errors
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    USER_AUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 인증 정보를 찾을 수 없습니다."),

    // OAuth2 Provider Specific Errors
    PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다."),
    PROVIDER_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "OAuth2 프로바이더 토큰이 만료되었습니다."),
    PROVIDER_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "OAuth2 프로바이더 토큰 교환에 실패했습니다."),
    PRODIVDER_USER_INFO_FETCH_FAILED(HttpStatus.BAD_REQUEST, "OAuth2 사용자 정보 조회에 실패했습니다."),

    // 5xx : Server Errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "외부 서비스 호출에 실패했습니다."),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "요청 시간이 초과되었습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),
    COMCODE_NOT_FOUND(HttpStatus.NOT_FOUND, "공통 코드 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}