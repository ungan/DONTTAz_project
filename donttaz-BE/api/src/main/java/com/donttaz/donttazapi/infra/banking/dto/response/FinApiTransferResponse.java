package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiTransferResponse(
        String responseCode,
        String responseMessage,
        String transactionId,
        String depositAccountName,
        String depositBankName,
        Integer amount,
        Integer fee,
        Integer withdrawalBalance
) {
}
