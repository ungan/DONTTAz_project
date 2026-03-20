package com.donttaz.donttazapi.account.service.dto;

public record OneWonVerificationRequestCommand(
        String accountNo,
        String authText
) {
}
