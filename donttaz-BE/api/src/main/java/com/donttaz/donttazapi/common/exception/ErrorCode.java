package com.donttaz.donttazapi.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", "계좌를 찾을 수 없습니다."),
    ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS", "이미 등록된 계좌입니다."),
    TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND", "거래 내역을 찾을 수 없습니다."),
    INVALID_BANK_CODE(HttpStatus.BAD_REQUEST, "INVALID_BANK_CODE", "유효하지 않은 은행 코드입니다."),
    INVALID_CONSENT_TOKEN(HttpStatus.BAD_REQUEST, "INVALID_CONSENT_TOKEN", "유효하지 않은 동의 토큰입니다."),
    INVALID_TRANSACTION_TYPE(HttpStatus.BAD_REQUEST, "INVALID_TRANSACTION_TYPE", "유효하지 않은 거래 유형입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    FIN_API_REQUEST_FAILED(HttpStatus.BAD_REQUEST, "FIN_API_REQUEST_FAILED", "finAPI 요청에 실패했습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    USER_KEY_ISSUANCE_FAILED(HttpStatus.BAD_REQUEST, "USER_KEY_ISSUANCE_FAILED", "사용자 키 발급에 실패했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증이 필요합니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 갱신 토큰입니다."),
    IDENTITY_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "IDENTITY_VERIFICATION_FAILED", "본인인증 검증에 실패했습니다."),
    VAULT_NOT_FOUND(HttpStatus.NOT_FOUND, "VAULT_NOT_FOUND", "금고를 찾을 수 없습니다."),
    VAULT_ALREADY_EXISTS(HttpStatus.CONFLICT, "VAULT_ALREADY_EXISTS", "이미 기본 금고가 존재합니다."),
    VAULT_ALREADY_ACHIEVED(HttpStatus.CONFLICT, "VAULT_ALREADY_ACHIEVED", "이미 목표가 달성된 금고입니다."),
    VAULT_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "VAULT_INSUFFICIENT_BALANCE", "금고 잔액이 부족합니다."),
    VAULT_GOAL_NOT_MET(HttpStatus.BAD_REQUEST, "VAULT_GOAL_NOT_MET", "목표 금액에 도달하지 않았습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
