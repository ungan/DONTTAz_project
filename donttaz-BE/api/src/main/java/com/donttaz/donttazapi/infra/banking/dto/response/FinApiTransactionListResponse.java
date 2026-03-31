package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiTransactionListResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        FinApiTransactionListRec rec
) {
}
