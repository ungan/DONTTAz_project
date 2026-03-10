package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiOneWonConfirmResponse(
        String responseCode,
        String responseMessage,
        boolean verified,
        String consentToken
) {
}
