package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountTransferService {

    private static final Long DEMO_USER_ID = 1L;

    private final BankingClient bankingClient;

    public AccountTransferService(BankingClient bankingClient) {
        this.bankingClient = bankingClient;
    }

    public AccountDepositResponse deposit(String accountNo, AccountDepositRequest request) {
        AccountDepositResult result = bankingClient.deposit(
                resolveCurrentUserId(),
                new AccountDepositCommand(accountNo, request.transactionBalance(), request.transactionSummary())
        );

        return AccountDepositResponse.builder()
                .transactionUniqueNo(result.transactionUniqueNo())
                .transactionDate(result.transactionDate())
                .build();
    }

    public AccountWithdrawalResponse withdrawal(String accountNo, AccountWithdrawalRequest request) {
        AccountWithdrawalResult result = bankingClient.withdrawal(
                resolveCurrentUserId(),
                new AccountWithdrawalCommand(accountNo, request.transactionBalance(), request.transactionSummary())
        );

        return AccountWithdrawalResponse.builder()
                .transactionUniqueNo(result.transactionUniqueNo())
                .transactionDate(result.transactionDate())
                .build();
    }

    private Long resolveCurrentUserId() {
        return DEMO_USER_ID;
    }
}
