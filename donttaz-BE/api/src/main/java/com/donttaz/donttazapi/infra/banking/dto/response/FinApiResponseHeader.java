package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiResponseHeader(
        String responseCode,
        String responseMessage,
        String apiName,
        String transmissionDate,
        String transmissionTime,
        String institutionCode,
        String apiKey,
        String apiServiceCode,
        String institutionTransactionUniqueNo
) {
}
