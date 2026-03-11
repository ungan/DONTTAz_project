package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiAccountBalanceResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        FinApiDemandDepositAccountBalanceRec rec
) {
}
