package com.donttaz.donttazapi.infra.banking.dto.request;

import java.time.LocalDate;

public record FinApiTransactionListRequest(
        Long userId,
        String externalAccountId,
        LocalDate startDate,
        LocalDate endDate,
        String type,
        int page,
        int size
) {
}
