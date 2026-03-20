package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationConfirmCommand(
        String accountNo,
        String authText,
        String authCode
) {
}
