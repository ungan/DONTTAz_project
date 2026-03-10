package com.donttaz.donttazapi.account.service.dto;

public record TransferCommand(
        Long withdrawalAccountId,
        String depositBankCode,
        String depositAccountNumber,
        Integer amount,
        String withdrawalMemo,
        String depositMemo,
        String pinToken
) {
}
