package com.donttaz.donttazapi.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "공통 API 응답 포맷")
public class ApiResponse<T> {

    @Schema(description = "처리 상태", example = "SUCCESS")
    private String status;

    @Schema(description = "응답 메시지", example = "계좌 생성 성공")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>("FAIL", message, null);
    }
}
