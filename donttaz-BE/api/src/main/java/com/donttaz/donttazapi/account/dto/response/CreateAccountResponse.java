package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수시입출금 계좌 생성 결과")
public record CreateAccountResponse(
        @Schema(description = "내부 계좌 ID", example = "42")
        Long accountId,

        @Schema(description = "은행 코드", example = "001")
        String bankCode,

        @Schema(description = "생성된 계좌번호", example = "0016174648358792")
        String accountNo,

        @Schema(description = "통화 코드", example = "KRW")
        String currency,

        @Schema(description = "통화 이름", example = "원화")
        String currencyName
) {
}
