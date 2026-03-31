package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FinApiMemberResponse(
    @JsonProperty("userId") String userId,
    @JsonProperty("userName") String userName,
    @JsonProperty("institutionCode") String institutionCode,
    @JsonProperty("userKey") String userKey,
    @JsonProperty("created") String created,
    @JsonProperty("modified") String modified
) {
}
