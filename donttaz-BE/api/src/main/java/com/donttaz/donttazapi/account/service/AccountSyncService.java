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
            CurrentUserProvider currentUserProvider) {
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
                    toAmount(account.dailyTransferLimit()),
                    toAmount(account.oneTimeTransferLimit()),
                    syncedAt
            );
        }
    }

    public void syncCreatedAccount(String accountTypeUniqueNo, String accountNo) {
        syncAccountDetailSnapshot(resolveCurrentUserId(), bankingAccountQueryService.getAccountDetail(accountNo), accountTypeUniqueNo);
    }

    public Account syncAccountSnapshot(String accountNo) {
        return syncAccountSnapshot(resolveCurrentUserId(), accountNo);
    }

    public Account syncAccountSnapshot(Long userId, String accountNo) {
        return syncAccountDetailSnapshot(userId, bankingAccountQueryService.getAccountDetail(userId, accountNo), null);
    }

    public void syncAccountDetailSnapshot(AccountDetailResult account) {
        syncAccountDetailSnapshot(resolveCurrentUserId(), account, null);
    }

    public Account syncAccountDetailSnapshot(Long userId, AccountDetailResult account) {
        return syncAccountDetailSnapshot(userId, account, null);
    }

    public Account syncAccountDetailSnapshot(AccountDetailResult account, String accountTypeUniqueNo) {
        return syncAccountDetailSnapshot(resolveCurrentUserId(), account, accountTypeUniqueNo);
    }

    private Account syncAccountDetailSnapshot(Long userId, AccountDetailResult account, String accountTypeUniqueNo) {
        return upsertAccount(
                userId,
                account.bankCode(),
                account.bankName(),
                account.accountNo(),
                accountTypeUniqueNo,
                account.currency(),
                toBalance(account.accountBalance()),
                toAmount(account.dailyTransferLimit()),
                toAmount(account.oneTimeTransferLimit()),
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
            Long dailyTransferLimit,
            Long oneTimeTransferLimit,
            LocalDateTime syncedAt
    ) {
        Optional<Account> existing = accountRepository.findByUserIdAndBankCodeAndAccountNumber(
                userId,
                bankCode,
                accountNumber);

        if (existing.isPresent()) {
            Account account = existing.get();
            if (account.getStatus() == AccountStatus.UNLINKED) {
                return account;
            }
            account.synchronize(
                    bankName,
                    null,
                    accountTypeUniqueNo,
                    currency,
                    balance,
                    dailyTransferLimit,
                    oneTimeTransferLimit,
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
                        .dailyTransferLimit(dailyTransferLimit)
                        .oneTimeTransferLimit(oneTimeTransferLimit)
                        .linkedAt(syncedAt)
                        .lastSyncedAt(syncedAt)
                        .isDeleted(Boolean.FALSE)
                        .isPrimary(Boolean.FALSE)
                        .status(AccountStatus.ACTIVE)
                        .build());
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private Long toBalance(String balance) {
        return toAmount(balance);
    }

    private Long toAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }
}