package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.domain.TransactionType;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountTransferService {

    private final BankingClient bankingClient;
    private final AccountSyncService accountSyncService;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;

    public AccountTransferService(
            BankingClient bankingClient,
            AccountSyncService accountSyncService,
            TransactionRepository transactionRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.bankingClient = bankingClient;
        this.accountSyncService = accountSyncService;
        this.transactionRepository = transactionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    public AccountDepositResponse deposit(String accountNo, AccountDepositRequest request) {
        AccountDepositResult result = bankingClient.deposit(
                resolveCurrentUserId(),
                new AccountDepositCommand(accountNo, request.transactionBalance(), request.transactionSummary())
        );
        persistTransaction(accountNo, TransactionType.DEPOSIT, request.transactionBalance(), request.transactionSummary(), result.transactionDate());

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
        persistTransaction(accountNo, TransactionType.WITHDRAWAL, request.transactionBalance(), request.transactionSummary(), result.transactionDate());

        return AccountWithdrawalResponse.builder()
                .transactionUniqueNo(result.transactionUniqueNo())
                .transactionDate(result.transactionDate())
                .build();
    }

    private void persistTransaction(
            String accountNo,
            TransactionType type,
            String amount,
            String summary,
            String transactionDate
    ) {
        Account account = accountSyncService.syncAccountSnapshot(accountNo);
        transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .type(type)
                        .amount(toInt(amount))
                        .balanceAfter(toInt(account.getBalance()))
                        .counterpartyName("SELF")
                        .fee(0)
                        .category(type.name())
                        .memo(summary)
                        .transactionDate(toTransactionDate(transactionDate))
                        .build()
        );
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private int toInt(String value) {
        try {
            return Math.toIntExact(Long.parseLong(value));
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "거래 금액이 저장 범위를 초과합니다.");
        }
    }

    private int toInt(Long value) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException exception) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "계좌 잔액이 저장 범위를 초과합니다.");
        }
    }

    private LocalDateTime toTransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return LocalDate.parse(transactionDate, DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay();
        } catch (DateTimeParseException exception) {
            return LocalDateTime.now();
        }
    }
}
