package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiTransactionDetailResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        FinApiTransactionItemResponse rec
) {
}
