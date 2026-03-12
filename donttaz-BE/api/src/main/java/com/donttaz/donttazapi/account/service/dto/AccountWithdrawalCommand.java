package com.donttaz.donttazapi.account.service.dto;

public record AccountWithdrawalCommand(
        String accountNo,
        String transactionBalance,
        String transactionSummary
) {
}
