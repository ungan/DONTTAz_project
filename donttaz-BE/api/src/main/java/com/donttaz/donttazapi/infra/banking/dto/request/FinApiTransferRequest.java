package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiTransferRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String depositAccountNo,
        String depositTransactionSummary,
        String transactionBalance,
        String withdrawalAccountNo,
        String withdrawalTransactionSummary
) {
}
