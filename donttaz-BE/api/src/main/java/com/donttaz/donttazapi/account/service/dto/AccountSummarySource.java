package com.donttaz.donttazapi.account.service.dto;

public interface AccountSummarySource {

    String bankCode();

    String bankName();

    String userName();

    String accountNo();

    String accountName();

    String accountTypeCode();

    String accountTypeName();

    String accountCreatedDate();

    String accountExpiryDate();

    String dailyTransferLimit();

    String oneTimeTransferLimit();

    String accountBalance();

    String lastTransactionDate();

    String currency();
}
