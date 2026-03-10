package com.donttaz.donttazapi.account.service.dto;

import java.time.LocalDateTime;

public record AccountDetailResult(
        String externalAccountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance,
        LocalDateTime linkedAt
) {
}
