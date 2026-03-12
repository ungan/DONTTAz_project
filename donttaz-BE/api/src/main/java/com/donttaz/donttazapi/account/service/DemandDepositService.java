package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemandDepositService {

    private final BankingClient bankingClient;
    private final CurrentUserProvider currentUserProvider;

    public DemandDepositService(BankingClient bankingClient, CurrentUserProvider currentUserProvider) {
        this.bankingClient = bankingClient;
        this.currentUserProvider = currentUserProvider;
    }

    public DemandDepositProductResult createProduct(String bankCode, String accountName, String accountDescription) {
        return bankingClient.createDemandDepositProduct(bankCode, accountName, accountDescription);
    }

    public List<DemandDepositProductResult> getProducts() {
        return bankingClient.getDemandDepositProducts();
    }

    public DemandDepositAccountCreateResult createAccount(String accountTypeUniqueNo) {
        return bankingClient.createDemandDepositAccount(resolveCurrentUserId(), accountTypeUniqueNo);
    }

    public List<DemandDepositAccountResult> getAccounts() {
        return bankingClient.getDemandDepositAccounts(resolveCurrentUserId());
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }
}
