package com.donttaz.donttazapi.infra.banking.dto.request;

public record FinApiOneWonConfirmRequest(
        Long userId,
        String verificationId,
        String authCode
) {
}
