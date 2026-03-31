package com.donttaz.donttazapi.account.service.dto;

import java.util.List;

public record TransactionQueryResult(
        long totalElements,
        int totalPages,
        boolean last,
        List<TransactionSummaryResult> transactions
) {
}
