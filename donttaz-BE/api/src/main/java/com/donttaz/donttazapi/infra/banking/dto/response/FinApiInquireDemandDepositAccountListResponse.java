package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FinApiInquireDemandDepositAccountListResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        List<FinApiDemandDepositAccountRec> rec
) {
}
