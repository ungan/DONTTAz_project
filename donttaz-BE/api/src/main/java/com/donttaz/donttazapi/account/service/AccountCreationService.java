package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.vault.service.VaultCommandService;
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
    private final VaultCommandService vaultCommandService;
    private final com.donttaz.donttazapi.common.security.CurrentUserProvider currentUserProvider;

    public AccountCreationService(
            DemandDepositService demandDepositService,
            AccountSyncService accountSyncService,
            VaultCommandService vaultCommandService,
            com.donttaz.donttazapi.common.security.CurrentUserProvider currentUserProvider) {
        this.demandDepositService = demandDepositService;
        this.accountSyncService = accountSyncService;
        this.vaultCommandService = vaultCommandService;
        this.currentUserProvider = currentUserProvider;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        DemandDepositAccountCreateResult result = demandDepositService.createAccount(request.accountTypeUniqueNo());
        syncCreatedAccount(request, result);

        // 계좌 생성 성공 시 기본 금고 자동 생성
        Long userId = currentUserProvider.getCurrentUserId();
        vaultCommandService.createDefaultVault(userId);

        return CreateAccountResponse.builder()
                .bankCode(result.bankCode())
                .accountNo(result.accountNo())
                .currency(result.currency())
                .currencyName(result.currencyName())
                .build();
    }

    private void syncCreatedAccount(CreateAccountRequest request, DemandDepositAccountCreateResult result) {
        try {
            accountSyncService.syncCreatedAccount(request.accountTypeUniqueNo(), result.accountNo());
        } catch (RuntimeException exception) {
            log.warn(
                    "account sync failed after external account creation accountNo={} message={}",
                    result.accountNo(),
                    exception.getMessage());
        }
    }
}
