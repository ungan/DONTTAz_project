package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationRequestCommand(
        String bankCode,
        String accountNumber
) {
}
