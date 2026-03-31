package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiTransactionListRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo,
        String startDate,
        String endDate,
        String transactionType,
        String orderByType
) {
}
