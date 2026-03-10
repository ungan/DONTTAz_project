package com.donttaz.donttazapi.account.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AccountDetailResponse(
        Long accountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance,
        LocalDateTime linkedAt
) {
}
