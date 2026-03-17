package com.donttaz.donttazapi.common.exception;

import com.donttaz.donttazapi.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? ErrorCode.INVALID_REQUEST.getMessage()
                        : error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        log.warn("Invalid JSON request body: {}", exception.getMessage());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.fail("요청 본문 형식이 올바르지 않습니다. (JSON 형식을 확인해주세요)"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter: {}", exception.getParameterName());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.fail(String.format("'%s' 파라미터가 누락되었습니다.", exception.getParameterName())));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        log.warn("API End-point not found: {} {}", exception.getHttpMethod(), exception.getRequestURL());
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus()) // 400 대신 404를 내보내려면 추후 ErrorCode 추가 필요
                .body(ApiResponse.fail("요청하신 API 주소를 찾을 수 없습니다. URL 및 HTTP 메서드를 확인해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled Exception Occurred: ", exception); // 📌 스택 트레이스 로깅 추가
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
