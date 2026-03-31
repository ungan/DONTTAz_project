package com.donttaz.donttazapi.infra.banking.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FinApiInquireDemandDepositListResponse(
        @JsonProperty("Header")
        FinApiResponseHeader header,
        @JsonProperty("REC")
        List<FinApiDemandDepositProductRec> rec
) {
}
