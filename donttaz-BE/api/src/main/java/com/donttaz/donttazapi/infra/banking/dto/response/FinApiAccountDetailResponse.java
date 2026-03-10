package com.donttaz.donttazapi.infra.banking.dto.response;

import java.time.LocalDateTime;

public record FinApiAccountDetailResponse(
        String responseCode,
        String responseMessage,
        String accountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance,
        LocalDateTime linkedAt
) {
}
