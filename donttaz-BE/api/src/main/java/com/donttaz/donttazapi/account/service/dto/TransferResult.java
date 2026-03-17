package com.donttaz.donttazapi.account.service.dto;

import java.util.List;

public record TransferResult(
        List<TransferTransactionResult> transactions
) {
}
