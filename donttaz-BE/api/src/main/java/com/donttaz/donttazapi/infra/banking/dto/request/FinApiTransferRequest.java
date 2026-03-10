package com.donttaz.donttazapi.infra.banking.dto.request;

public record FinApiTransferRequest(
        Long userId,
        Long withdrawalAccountId,
        String depositBankCode,
        String depositAccountNumber,
        Integer amount,
        String withdrawalMemo,
        String depositMemo,
        String pinToken
) {
}
