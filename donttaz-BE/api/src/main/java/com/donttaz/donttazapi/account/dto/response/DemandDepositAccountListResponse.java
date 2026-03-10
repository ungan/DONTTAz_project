package com.donttaz.donttazapi.account.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record DemandDepositAccountListResponse(
        List<DemandDepositAccountResponse> accounts
) {
}
