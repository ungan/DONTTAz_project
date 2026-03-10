package com.donttaz.donttazapi.account.dto.response;

import lombok.Builder;

@Builder
public record AccountSummaryResponse(
        Long accountId,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance
) {
}
