package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationConfirmCommand(
        String verificationId,
        String authCode
) {
}
