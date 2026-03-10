package com.donttaz.donttazapi.infra.banking.dto.response;

import java.util.List;

public record FinApiAccountListResponse(
        String responseCode,
        String responseMessage,
        List<FinApiAccountItemResponse> accounts
) {
}
