package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiAccountItemResponse(
        String accountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance
) {
}
