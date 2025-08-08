package com.climbx.climbx.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum ErrorCode {

    /**
     * Common errors
     */
    // 4xx: Client errors
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "유효하지 않은 파라미터입니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "유효성 검사에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "필수 파라미터가 누락되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "메서드 파라미터 타입이 일치하지 않습니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청 허용 횟수를 초과했습니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 파일 크기가 5MB 제한을 초과했습니다."),

    // 5xx: Server errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "외부 서비스 호출에 실패했습니다."),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "요청 시간이 초과되었습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다."),
    INVALID_ENUM_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 Enum 값입니다."),

    /**
     * User errors
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_STAT_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 통계 정보를 찾을 수 없습니다."),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    NICKNAME_MISMATCH(HttpStatus.FORBIDDEN, "닉네임이 일치하지 않습니다."),
    VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "비디오를 찾을 수 없습니다."),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."),
    FORBIDDEN_SUBMISSION(HttpStatus.FORBIDDEN, "제출물에 대한 권한이 없습니다."),
    FORBIDDEN_PROBLEM_VOTE(HttpStatus.FORBIDDEN, "해당 문제 투표에 대한 권한이 없습니다."),
    DUPLICATE_APPEAL(HttpStatus.CONFLICT, "이미 이의신청이 접수된 제출물입니다."),
    DUPLICATE_SUBMISSION(HttpStatus.CONFLICT, "이미 제출된 영상입니다."),
    DEFAULT_NICKNAME_RETRY_LIMIT_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR,
        "기본 닉네임 생성 재시도 횟수를 초과했습니다."),

    /**
     * Gym errors
     */
    GYM_NOT_FOUND(HttpStatus.NOT_FOUND, "클라이밍장을 찾을 수 없습니다."),
    GYM_AREA_NOT_FOUND(HttpStatus.NOT_FOUND, "클라이밍장 벽(구역)을 찾을 수 없습니다."),

    /**
     * Ranking errors
     */
    INVALID_RANKING_CRITERIA(HttpStatus.BAD_REQUEST, "유효하지 않은 랭킹 기준입니다."),

    /**
     * Rating Util errors
     */
    INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 레이팅 값입니다. 0이상 3100 이하의 정수 값을 입력해주세요."),

    /**
     * Submission errors
     */
    PENDING_SUBMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PENDING 상태인 제출물을 찾을 수 없습니다."),
    STATUS_MODIFY_TO_PENDING(HttpStatus.BAD_REQUEST, "제출물 상태를 PENDING으로 변경할 수 없습니다."),

    /**
     * Auth errors
     */
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    INVALID_NONCE(HttpStatus.BAD_REQUEST, "유효하지 않은 nonce입니다."),
    USER_AUTH_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 인증 정보를 찾을 수 없습니다."),

    // OAuth2 Provider Specific Errors
    PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다."),
    PROVIDER_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "OAuth2 프로바이더 토큰이 만료되었습니다."),
    PROVIDER_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "OAuth2 프로바이더 토큰 교환에 실패했습니다."),
    PRODIVDER_USER_INFO_FETCH_FAILED(HttpStatus.BAD_REQUEST, "OAuth2 사용자 정보 조회에 실패했습니다."),
    PUBLIC_KEY_FETCH_FAILED(HttpStatus.BAD_REQUEST, "공개키 조회에 실패했습니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "OAuth2 프로바이더 이메일이 인증되지 않았습니다."),

    /**
     * AWS errors
     */
    S3_BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "S3 버킷을 찾을 수 없습니다."),
    FILE_EXTENSION_NOT_EXISTS(HttpStatus.BAD_REQUEST, "파일 확장자가 존재하지 않습니다."),
    S3_BUCKET_NAME_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷 이름이 설정되지 않았습니다."),
    CLOUDFRONT_DOMAIN_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR,
        "CloudFront 도메인이 설정되지 않았습니다."),
    S3_FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    public int getStatusCode() {
        return status.value();
    }

    public String getMessage() {
        return message;
    }
}