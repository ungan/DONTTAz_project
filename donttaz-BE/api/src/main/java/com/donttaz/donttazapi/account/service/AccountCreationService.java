package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountCreationService {

    private static final Logger log = LoggerFactory.getLogger(AccountCreationService.class);

    private final DemandDepositService demandDepositService;
    private final AccountSyncService accountSyncService;

    public AccountCreationService(
            DemandDepositService demandDepositService,
            AccountSyncService accountSyncService) {
        this.demandDepositService = demandDepositService;
        this.accountSyncService = accountSyncService;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        DemandDepositAccountCreateResult result = demandDepositService.createAccount(request.accountTypeUniqueNo());
        Long accountId = syncCreatedAccount(request, result);

        return CreateAccountResponse.builder()
                .accountId(accountId)
                .bankCode(result.bankCode())
                .accountNo(result.accountNo())
                .currency(result.currency())
                .currencyName(result.currencyName())
                .build();
    }

    private Long syncCreatedAccount(CreateAccountRequest request, DemandDepositAccountCreateResult result) {
        try {
            Account account = accountSyncService.syncCreatedAccount(request.accountTypeUniqueNo(), result.accountNo());
            return account.getAccountId();
        } catch (RuntimeException exception) {
            log.warn(
                    "account sync failed after external account creation accountNo={} message={}",
                    result.accountNo(),
                    exception.getMessage());
            return null;
        }
    }
}
