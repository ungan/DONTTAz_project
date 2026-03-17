package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "계좌 이체 결과")
public record AccountTransferResponse(
        @Schema(description = "이체 결과 거래 목록")
        List<AccountTransferItemResponse> transactions
) {
}
