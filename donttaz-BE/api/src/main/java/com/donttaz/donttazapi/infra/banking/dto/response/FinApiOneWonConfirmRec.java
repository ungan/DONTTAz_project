package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiOneWonConfirmRec(
        String status,
        String transactionUniqueNo,
        String accountNo
) {
}
