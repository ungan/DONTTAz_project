package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.request.AccountCloseRequest;
import com.donttaz.donttazapi.account.dto.response.AccountCloseResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.AccountCloseCommand;
import com.donttaz.donttazapi.account.service.dto.AccountCloseResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountManagementService {

    private final AccountRepository accountRepository;
    private final BankingClient bankingClient;
    private final CurrentUserProvider currentUserProvider;

    public AccountManagementService(
            AccountRepository accountRepository,
            BankingClient bankingClient,
            CurrentUserProvider currentUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.bankingClient = bankingClient;
        this.currentUserProvider = currentUserProvider;
    }

    public void setPrimaryAccount(String accountNo) {
        Long userId = resolveCurrentUserId();
        Account account = getSingleActiveAccount(userId, accountNo);

        accountRepository.clearPrimaryByUserId(userId);
        account.markAsPrimary();
        accountRepository.save(account);
    }

    public AccountCloseResponse closeAccount(String bankCode, String accountNo, AccountCloseRequest request) {
        Long userId = resolveCurrentUserId();
        Account account = getActiveAccount(userId, bankCode, accountNo);
        Account refundAccount = getActiveAccount(userId, request.refundBankCode(), request.refundAccountNo());
        validateRefundAccount(account, refundAccount);

        AccountCloseResult result = bankingClient.closeAccount(
                userId,
                new AccountCloseCommand(accountNo, request.refundAccountNo())
        );

        account.unlink(parseBalance(result.accountBalance()), LocalDateTime.now());
        accountRepository.save(account);

        return AccountCloseResponse.builder()
                .status(result.status())
                .accountNo(result.accountNo())
                .refundAccountNo(result.refundAccountNo())
                .accountBalance(result.accountBalance())
                .build();
    }

    private Account getActiveAccount(Long userId, String bankCode, String accountNo) {
        return accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                        userId,
                        bankCode,
                        accountNo,
                        AccountStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private Account getSingleActiveAccount(Long userId, String accountNo) {
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
        return accounts.get(0);
    }

    private void validateRefundAccount(Account account, Account refundAccount) {
        if (account.getBankCode().equals(refundAccount.getBankCode())
                && account.getAccountNumber().equals(refundAccount.getAccountNumber())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private Long parseBalance(String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(
                    ErrorCode.FIN_API_REQUEST_FAILED,
                    "Close account response did not include a valid balance."
            );
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new CustomException(
                    ErrorCode.FIN_API_REQUEST_FAILED,
                    "Close account response returned a non-numeric balance."
            );
        }
    }
}
