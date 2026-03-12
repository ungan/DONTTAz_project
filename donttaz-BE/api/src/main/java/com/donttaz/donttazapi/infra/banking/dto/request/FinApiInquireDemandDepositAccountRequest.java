package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiInquireDemandDepositAccountRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo
) {
}
