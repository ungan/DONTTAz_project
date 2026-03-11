package com.donttaz.donttazapi.account.dto.response;

import lombok.Builder;

@Builder
public record AccountHolderNameResponse(
        String bankCode,
        String bankName,
        String accountNo,
        String userName,
        String currency
) {
}
