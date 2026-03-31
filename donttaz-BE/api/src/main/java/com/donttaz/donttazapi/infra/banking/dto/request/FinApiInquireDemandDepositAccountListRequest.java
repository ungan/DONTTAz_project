package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiInquireDemandDepositAccountListRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header
) {
}
