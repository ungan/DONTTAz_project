package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationRequestResult(
        String verificationId,
        String bankName,
        Integer expiredIn
) {
}
