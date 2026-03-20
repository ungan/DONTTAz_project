package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiOneWonConfirmRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo,
        String authText,
        String authCode
) {
}
