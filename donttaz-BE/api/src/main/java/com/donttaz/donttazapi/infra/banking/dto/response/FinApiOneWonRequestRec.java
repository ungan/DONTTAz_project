package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiOneWonRequestRec(
        String transactionUniqueNo,
        String accountNo
) {
}
