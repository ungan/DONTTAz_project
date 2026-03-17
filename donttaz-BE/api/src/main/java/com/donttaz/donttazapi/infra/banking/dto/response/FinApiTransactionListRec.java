package com.donttaz.donttazapi.infra.banking.dto.response;

import java.util.List;

public record FinApiTransactionListRec(
        String totalCount,
        List<FinApiTransactionItemResponse> list
) {
}
