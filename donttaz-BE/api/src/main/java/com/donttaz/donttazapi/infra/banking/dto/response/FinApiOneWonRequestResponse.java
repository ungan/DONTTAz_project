package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiOneWonRequestResponse(
        String responseCode,
        String responseMessage,
        String verificationId,
        String bankName,
        Integer expiredIn
) {
}
