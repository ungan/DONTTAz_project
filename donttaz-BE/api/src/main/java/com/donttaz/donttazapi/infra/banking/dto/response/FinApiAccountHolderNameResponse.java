package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiAccountHolderNameResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        FinApiDemandDepositAccountHolderNameRec rec
) {
}
