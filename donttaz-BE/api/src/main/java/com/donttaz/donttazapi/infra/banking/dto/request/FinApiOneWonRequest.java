package com.donttaz.donttazapi.infra.banking.dto.request;

public record FinApiOneWonRequest(
        Long userId,
        String bankCode,
        String accountNumber
) {
}
