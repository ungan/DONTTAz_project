package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Account close result")
public record AccountCloseResponse(
        @Schema(description = "Closed account status", example = "CLOSED")
        String status,

        @Schema(description = "Closed account number", example = "0018770964252220")
        String accountNo,

        @Schema(description = "Refund account number", example = "0324003842129948")
        String refundAccountNo,

        @Schema(description = "Closed account balance", example = "0")
        String accountBalance
) {
}
