package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiDeleteDemandDepositAccountRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo,
        String refundAccountNo
) {
}
