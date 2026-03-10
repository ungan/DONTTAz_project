package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "수시입출금 계좌 목록 응답")
public record AccountListResponse(
        @Schema(description = "전체 계좌 잔액 합계", example = "8003477")
        Long totalBalance,

        @Schema(description = "계좌 목록")
        List<AccountSummaryResponse> accountList
) {
}
