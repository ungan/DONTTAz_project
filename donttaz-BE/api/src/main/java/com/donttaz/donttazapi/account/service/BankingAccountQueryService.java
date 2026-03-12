package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BankingAccountQueryService {

    private final BankingClient bankingClient;
    private final CurrentUserProvider currentUserProvider;

    public BankingAccountQueryService(BankingClient bankingClient, CurrentUserProvider currentUserProvider) {
        this.bankingClient = bankingClient;
        this.currentUserProvider = currentUserProvider;
    }

    public AccountBalanceResult getAccountBalance(String accountNo) {
        return bankingClient.getAccountBalance(resolveCurrentUserId(), accountNo);
    }

    public AccountDetailResult getAccountDetail(String accountNo) {
        return bankingClient.getAccountDetail(resolveCurrentUserId(), accountNo);
    }

    public AccountHolderNameResult getAccountHolderName(String accountNo) {
        return bankingClient.getAccountHolderName(resolveCurrentUserId(), accountNo);
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }
}
