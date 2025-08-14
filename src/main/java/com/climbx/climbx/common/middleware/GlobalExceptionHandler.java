package com.climbx.climbx.common.middleware;

import com.climbx.climbx.common.dto.ApiResponseDto;
import com.climbx.climbx.common.enums.ErrorCode;
import com.climbx.climbx.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * 비즈니스 로직에서 발생하는 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBusinessException(BusinessException e) {
        log.warn(
            "BusinessException occurred: code={}, message={}, context={}",
            e.errorCode().getStatusCode(),
            e.getMessage(),
            e.context(),
            e
        );
        ApiResponseDto<Void> response = ApiResponseDto.error(
            e.errorCode().status(),
            e.errorCode().message() + (e.context().isEmpty() ? "" : " - " + e.context())
        );
        return ResponseEntity
            .status(e.errorCode().status())
            .body(response);
    }

    /*
     * Validation 관련 예외 처리
     */
    @ExceptionHandler({
        ConstraintViolationException.class,
        MethodArgumentNotValidException.class,
        MissingServletRequestParameterException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponseDto<Void>> handleValidationException(Exception e) {
        String errorMessage = extractValidationMessage(e);
        ErrorCode errorCode = chooseErrorCode(e);

        log.warn(
            "ValidationException occurred: code={}, message={}",
            errorCode.getStatusCode(),
            errorMessage,
            e
        );

        ApiResponseDto<Void> response = ApiResponseDto.error(
            errorCode.status(),
            errorMessage
        );

        return ResponseEntity
            .status(errorCode.status())
            .body(response);
    }

    /*
     * MethodArgumentTypeMismatchException 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException e
    ) {
        ErrorCode errorCode = chooseErrorCode(e);
        log.warn(
            "MethodArgumentTypeMismatchException occurred: code={}, message={}",
            errorCode.getStatusCode(),
            e.getMessage(),
            e
        );

        ApiResponseDto<Void> response = ApiResponseDto.error(
            errorCode.status(),
            errorCode.getMessage()
        );

        return ResponseEntity
            .status(errorCode.status())
            .body(response);
    }

    /*
     * 정의되지 않은 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(Exception e) {

        ErrorCode errorCode = chooseErrorCode(e);
        log.error(
            "UnexpectedException occurred: code={}, message={}",
            errorCode.getStatusCode(),
            e.getMessage(),
            e
        );

        ApiResponseDto<Void> response = ApiResponseDto.error(
            errorCode.status(),
            errorCode.getMessage()
        );

        return ResponseEntity
            .status(errorCode.status())
            .body(response);
    }

    private String extractValidationMessage(Exception e) {
        if (e instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + "(" + violation.getMessage() + ")")
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
        } else if (e instanceof MethodArgumentNotValidException manve) {
            return manve.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + "(" + error.getDefaultMessage() + ")")
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation failed");
        } else if (e instanceof MissingServletRequestParameterException msrpe) {
            return "Missing required parameter(" + msrpe.getParameterName() + ")";
        } else if (e instanceof IllegalArgumentException iae) {
            return "Illegal argument: " + iae.getMessage();
        }
        return "Validation failed";
    }

    private ErrorCode chooseErrorCode(Exception e) {
        if (e instanceof ConstraintViolationException) {
            return ErrorCode.VALIDATION_FAILED;
        } else if (e instanceof MethodArgumentNotValidException) {
            return ErrorCode.VALIDATION_FAILED;
        } else if (e instanceof MissingServletRequestParameterException) {
            return ErrorCode.MISSING_REQUEST_PARAMETER;
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            return ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
        } else if (e instanceof IllegalArgumentException) {
            return ErrorCode.INVALID_PARAMETER;
        }
        return ErrorCode.INTERNAL_ERROR;
    }
}
