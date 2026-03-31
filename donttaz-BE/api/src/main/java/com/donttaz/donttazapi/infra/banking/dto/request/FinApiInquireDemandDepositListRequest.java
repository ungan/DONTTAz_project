package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiInquireDemandDepositListRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header
) {
}
