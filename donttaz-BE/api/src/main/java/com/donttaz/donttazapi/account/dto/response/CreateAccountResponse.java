package com.donttaz.donttazapi.account.dto.response;

import lombok.Builder;

@Builder
public record CreateAccountResponse(
        Long accountId,
        String bankName,
        Integer balance
) {
}
