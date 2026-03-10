package com.donttaz.donttazapi.account.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record AccountListResponse(
        Integer totalBalance,
        List<AccountSummaryResponse> accountList
) {
}
