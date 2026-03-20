package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationRequestResult(
        String transactionUniqueNo,
        String accountNo
) {
}
