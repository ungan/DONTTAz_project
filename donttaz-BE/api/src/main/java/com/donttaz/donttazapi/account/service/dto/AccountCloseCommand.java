package com.donttaz.donttazapi.account.service.dto;

public record AccountCloseCommand(
        String accountNo,
        String refundAccountNo
) {
}
