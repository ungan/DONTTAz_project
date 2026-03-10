package com.donttaz.donttazapi.account.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record TransactionListResponse(
        long totalElements,
        int totalPages,
        boolean isLast,
        List<TransactionSummaryResponse> transactions
) {
}
