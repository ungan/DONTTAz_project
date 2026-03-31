package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiOneWonConfirmResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        FinApiOneWonConfirmRec rec
) {
}
