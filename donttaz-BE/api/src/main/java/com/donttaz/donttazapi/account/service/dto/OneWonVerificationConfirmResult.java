package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationConfirmResult(
        boolean verified,
        String consentToken
) {
}
