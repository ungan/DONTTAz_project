package com.donttaz.donttazapi.company.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 전체 계좌 목록 요약 (관리자/테스트 용도)")
public record UserAccountSummaryResponse(
        @Schema(description = "사용자 ID", example = "1") Long userId,

        @Schema(description = "사용자 이름(또는 닉네임)", example = "홍길동") String userName,

        @Schema(description = "사용자의 계좌 번호", example = "001234567890") String accountNumber) {
}
