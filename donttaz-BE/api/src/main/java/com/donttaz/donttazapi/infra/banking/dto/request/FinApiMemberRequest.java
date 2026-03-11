package com.donttaz.donttazapi.infra.banking.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiMemberRequest(
    @JsonProperty("apiKey") String apiKey,
    @JsonProperty("userId") String userId
) {
}
