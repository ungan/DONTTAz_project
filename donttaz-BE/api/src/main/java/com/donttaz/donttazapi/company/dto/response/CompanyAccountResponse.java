package com.donttaz.donttazapi.company.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기업 계좌 정보 응답")
public record CompanyAccountResponse(
        @Schema(description = "기업 계좌 ID", example = "1") Long companyAccountId,

        @Schema(description = "사용자 ID (금융 API 연동용)", example = "100") Long userId,

        @Schema(description = "계좌 번호", example = "1111111111") String accountNumber) {
}
