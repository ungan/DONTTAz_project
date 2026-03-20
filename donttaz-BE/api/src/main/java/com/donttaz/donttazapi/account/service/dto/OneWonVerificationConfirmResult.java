package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationConfirmResult(
        String status,
        boolean verified,
        String transactionUniqueNo,
        String accountNo
) {
}
