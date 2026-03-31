package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiTransferLimitUpdateRequest(
        @JsonProperty("Header")
        FinApiRequestHeader header,
        String accountNo,
        String oneTimeTransferLimit,
        String dailyTransferLimit
) {
}
