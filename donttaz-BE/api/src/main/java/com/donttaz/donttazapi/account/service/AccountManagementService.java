package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountManagementService {

    private final AccountRepository accountRepository;
    private final CurrentUserProvider currentUserProvider;

    public AccountManagementService(
            AccountRepository accountRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public void setPrimaryAccount(String accountNo) {
        Long userId = currentUserProvider.getCurrentUserId();
        List<Account> accounts = accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(
                userId,
                accountNo,
                AccountStatus.ACTIVE
        );
        if (accounts.isEmpty()) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (accounts.size() > 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Account account = accounts.get(0);

        accountRepository.clearPrimaryByUserId(userId);
        account.markAsPrimary();
        accountRepository.save(account);
    }
}
