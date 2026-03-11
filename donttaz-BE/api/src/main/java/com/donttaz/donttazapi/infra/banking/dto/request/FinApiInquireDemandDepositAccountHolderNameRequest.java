package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiInquireDemandDepositAccountHolderNameRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo
) {
}
