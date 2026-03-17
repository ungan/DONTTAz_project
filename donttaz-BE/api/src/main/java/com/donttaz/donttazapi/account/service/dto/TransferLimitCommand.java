package com.donttaz.donttazapi.account.service.dto;

public record TransferLimitCommand(
        String accountNo,
        String oneTimeTransferLimit,
        String dailyTransferLimit
) {
}
