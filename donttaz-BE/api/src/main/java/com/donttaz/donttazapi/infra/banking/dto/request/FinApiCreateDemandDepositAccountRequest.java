package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiCreateDemandDepositAccountRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountTypeUniqueNo
) {
}
