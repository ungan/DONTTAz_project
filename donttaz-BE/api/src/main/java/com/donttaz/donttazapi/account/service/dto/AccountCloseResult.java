package com.donttaz.donttazapi.account.service.dto;

public record AccountCloseResult(
        String status,
        String accountNo,
        String refundAccountNo,
        String accountBalance
) {
}
