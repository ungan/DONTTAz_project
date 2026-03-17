package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.domain.TransactionType;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferItemResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferLimitCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountTransferService {

    private static final Logger log = LoggerFactory.getLogger(AccountTransferService.class);

    private final BankingClient bankingClient;
    private final AccountSyncService accountSyncService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;

    public AccountTransferService(
            BankingClient bankingClient,
            AccountSyncService accountSyncService,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.bankingClient = bankingClient;
        this.accountSyncService = accountSyncService;
        this.accountRepository = accountRepository;
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

    public AccountTransferResponse transfer(AccountTransferRequest request) {
        TransferResult result = bankingClient.transfer(
                resolveCurrentUserId(),
                new TransferCommand(
                        request.depositAccountNo(),
                        request.depositTransactionSummary(),
                        request.transactionBalance(),
                        request.withdrawalAccountNo(),
                        request.withdrawalTransactionSummary()
                )
        );
        persistTransferTransactions(request, result);

        return AccountTransferResponse.builder()
                .transactions(
                        result.transactions().stream()
                                .map(this::toAccountTransferItemResponse)
                                .toList()
                )
                .build();
    }

    public AccountTransferLimitUpdateResponse updateTransferLimit(
            String accountNo,
            AccountTransferLimitUpdateRequest request
    ) {
        AccountDetailResult result = bankingClient.updateTransferLimit(
                resolveCurrentUserId(),
                new TransferLimitCommand(
                        accountNo,
                        request.oneTimeTransferLimit(),
                        request.dailyTransferLimit()
                )
        );
        accountSyncService.syncAccountDetailSnapshot(result);

        return AccountTransferLimitUpdateResponse.builder()
                .bankCode(result.bankCode())
                .bankName(result.bankName())
                .accountNo(result.accountNo())
                .accountName(result.accountName())
                .dailyTransferLimit(result.dailyTransferLimit())
                .oneTimeTransferLimit(result.oneTimeTransferLimit())
                .accountBalance(result.accountBalance())
                .lastTransactionDate(result.lastTransactionDate())
                .currency(result.currency())
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

    private void persistTransferTransactions(AccountTransferRequest request, TransferResult result) {
        try {
            Account withdrawalAccount = accountSyncService.syncAccountSnapshot(request.withdrawalAccountNo());
            saveTransferTransaction(
                    withdrawalAccount,
                    findTransactionByAccountNo(result.transactions(), request.withdrawalAccountNo()).orElse(null),
                    TransactionType.WITHDRAWAL,
                    request.transactionBalance(),
                    request.withdrawalTransactionSummary(),
                    request.depositAccountNo()
            );

            syncOwnedDepositAccount(request.depositAccountNo())
                    .ifPresent(account -> saveTransferTransaction(
                            account,
                            findTransactionByAccountNo(result.transactions(), request.depositAccountNo()).orElse(null),
                            TransactionType.DEPOSIT,
                            request.transactionBalance(),
                            request.depositTransactionSummary(),
                            request.withdrawalAccountNo()
                    ));
        } catch (RuntimeException exception) {
            log.warn(
                    "transfer persistence failed withdrawalAccountNo={} depositAccountNo={} message={}",
                    request.withdrawalAccountNo(),
                    request.depositAccountNo(),
                    exception.getMessage()
            );
        }
    }

    private Optional<Account> syncOwnedDepositAccount(String accountNo) {
        if (accountRepository.findByUserIdAndAccountNumberAndIsDeletedFalse(resolveCurrentUserId(), accountNo).isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(accountSyncService.syncAccountSnapshot(accountNo));
        } catch (RuntimeException exception) {
            log.warn("deposit account snapshot sync skipped accountNo={} message={}", accountNo, exception.getMessage());
            return Optional.empty();
        }
    }

    private void saveTransferTransaction(
            Account account,
            TransferTransactionResult transactionResult,
            TransactionType type,
            String amount,
            String summary,
            String counterpartyAccountNo
    ) {
        transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .type(type)
                        .amount(toInt(amount))
                        .balanceAfter(toInt(account.getBalance()))
                        .counterpartyName("TRANSFER")
                        .counterpartyAccount(counterpartyAccountNo)
                        .fee(0)
                        .category(type.name())
                        .memo(summary)
                        .transactionDate(toTransactionDate(transactionResult == null ? null : transactionResult.transactionDate()))
                        .build()
        );
    }

    private Optional<TransferTransactionResult> findTransactionByAccountNo(List<TransferTransactionResult> transactions, String accountNo) {
        return transactions.stream()
                .filter(transaction -> accountNo.equals(transaction.accountNo()))
                .findFirst();
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

    private AccountTransferItemResponse toAccountTransferItemResponse(TransferTransactionResult result) {
        return AccountTransferItemResponse.builder()
                .transactionUniqueNo(result.transactionUniqueNo())
                .accountNo(result.accountNo())
                .transactionDate(result.transactionDate())
                .transactionType(result.transactionType())
                .transactionTypeName(result.transactionTypeName())
                .transactionAccountNo(result.transactionAccountNo())
                .build();
    }
}
