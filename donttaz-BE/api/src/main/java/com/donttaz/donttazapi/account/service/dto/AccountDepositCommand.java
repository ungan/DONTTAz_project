package com.donttaz.donttazapi.account.service.dto;

public record AccountDepositCommand(
        String accountNo,
        String transactionBalance,
        String transactionSummary
) {
}
