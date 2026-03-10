package com.donttaz.donttazapi.infra.banking.dto.response;

import java.util.List;

public record FinApiTransactionListResponse(
        String responseCode,
        String responseMessage,
        long totalElements,
        int totalPages,
        boolean last,
        List<FinApiTransactionItemResponse> transactions
) {
}
