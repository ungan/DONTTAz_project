package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiDeleteDemandDepositAccountRec(
        String status,
        String accountNo,
        String refundAccountNo,
        String accountBalance
) {
}
