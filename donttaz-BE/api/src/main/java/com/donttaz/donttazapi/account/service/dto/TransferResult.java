package com.donttaz.donttazapi.account.service.dto;

public record TransferResult(
        String externalTransactionId,
        String depositAccountName,
        String depositBankName,
        Integer amount,
        Integer fee,
        Integer withdrawalBalance
) {
}
