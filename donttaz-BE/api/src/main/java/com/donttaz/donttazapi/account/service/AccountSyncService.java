package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountSyncService {

    private final AccountRepository accountRepository;
    private final BankingAccountQueryService bankingAccountQueryService;
    private final CurrentUserProvider currentUserProvider;

    public AccountSyncService(
            AccountRepository accountRepository,
            BankingAccountQueryService bankingAccountQueryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.bankingAccountQueryService = bankingAccountQueryService;
        this.currentUserProvider = currentUserProvider;
    }

    public void syncDemandDepositAccounts(List<DemandDepositAccountResult> accounts) {
        Long userId = resolveCurrentUserId();
        LocalDateTime syncedAt = LocalDateTime.now();

        for (DemandDepositAccountResult account : accounts) {
            upsertAccount(
                    userId,
                    account.bankCode(),
                    account.bankName(),
                    account.accountNo(),
                    null,
                    account.currency(),
                    toBalance(account.accountBalance()),
                    syncedAt
            );
        }
    }

    public void syncCreatedAccount(String accountTypeUniqueNo, String accountNo) {
        AccountDetailResult account = bankingAccountQueryService.getAccountDetail(accountNo);

        upsertAccount(
                resolveCurrentUserId(),
                account.bankCode(),
                account.bankName(),
                account.accountNo(),
                accountTypeUniqueNo,
                account.currency(),
                toBalance(account.accountBalance()),
                LocalDateTime.now()
        );
    }

    public Account syncAccountSnapshot(String accountNo) {
        AccountDetailResult account = bankingAccountQueryService.getAccountDetail(accountNo);

        return upsertAccount(
                resolveCurrentUserId(),
                account.bankCode(),
                account.bankName(),
                account.accountNo(),
                null,
                account.currency(),
                toBalance(account.accountBalance()),
                LocalDateTime.now()
        );
    }

    private Account upsertAccount(
            Long userId,
            String bankCode,
            String bankName,
            String accountNumber,
            String accountTypeUniqueNo,
            String currency,
            Long balance,
            LocalDateTime syncedAt
    ) {
        Optional<Account> existing = accountRepository.findByUserIdAndBankCodeAndAccountNumber(
                userId,
                bankCode,
                accountNumber
        );

        if (existing.isPresent()) {
            Account account = existing.get();
            account.synchronize(
                    bankName,
                    null,
                    accountTypeUniqueNo,
                    currency,
                    balance,
                    syncedAt
            );
            return account;
        }

        return accountRepository.save(
                Account.builder()
                        .userId(userId)
                        .bankCode(bankCode)
                        .bankName(bankName)
                        .accountNumber(accountNumber)
                        .accountTypeUniqueNo(accountTypeUniqueNo)
                        .currency(currency)
                        .balance(balance)
                        .linkedAt(syncedAt)
                        .lastSyncedAt(syncedAt)
                        .isDeleted(Boolean.FALSE)
                        .status(AccountStatus.ACTIVE)
                        .build()
        );
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private Long toBalance(String balance) {
        if (balance == null || balance.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(balance);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}
