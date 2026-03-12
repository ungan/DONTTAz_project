package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiAccountDepositRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo,
        String transactionBalance,
        String transactionSummary
) {
}
