package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.domain.TransactionType;
import com.donttaz.donttazapi.account.dto.response.AccountBalanceResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountHolderNameResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.AccountSummaryResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionSummaryResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private static final Long DEMO_USER_ID = 1L;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DemandDepositService demandDepositService;
    private final BankingAccountQueryService bankingAccountQueryService;

    public AccountQueryService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            DemandDepositService demandDepositService,
            BankingAccountQueryService bankingAccountQueryService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.demandDepositService = demandDepositService;
        this.bankingAccountQueryService = bankingAccountQueryService;
    }

    public AccountListResponse getAccounts() {
        List<DemandDepositAccountResult> accounts = demandDepositService.getAccounts();

        List<AccountSummaryResponse> accountList = accounts.stream()
                .map(this::toAccountSummaryResponse)
                .toList();

        long totalBalance = accounts.stream()
                .mapToLong(this::toLongBalance)
                .sum();

        return AccountListResponse.builder()
                .totalBalance(totalBalance)
                .accountList(accountList)
                .build();
    }

    public AccountBalanceResponse getAccountBalance(String accountNo) {
        AccountBalanceResult account = bankingAccountQueryService.getAccountBalance(accountNo);

        return AccountBalanceResponse.builder()
                .bankCode(account.bankCode())
                .accountNo(account.accountNo())
                .accountBalance(account.accountBalance())
                .accountCreatedDate(account.accountCreatedDate())
                .accountExpiryDate(account.accountExpiryDate())
                .lastTransactionDate(account.lastTransactionDate())
                .currency(account.currency())
                .build();
    }

    public AccountDetailResponse getAccountDetail(String accountNo) {
        AccountDetailResult account = bankingAccountQueryService.getAccountDetail(accountNo);

        return AccountDetailResponse.builder()
                .bankCode(account.bankCode())
                .bankName(account.bankName())
                .userName(account.userName())
                .accountNo(account.accountNo())
                .accountName(account.accountName())
                .accountTypeCode(account.accountTypeCode())
                .accountTypeName(account.accountTypeName())
                .accountCreatedDate(account.accountCreatedDate())
                .accountExpiryDate(account.accountExpiryDate())
                .dailyTransferLimit(account.dailyTransferLimit())
                .oneTimeTransferLimit(account.oneTimeTransferLimit())
                .accountBalance(account.accountBalance())
                .lastTransactionDate(account.lastTransactionDate())
                .currency(account.currency())
                .build();
    }

    public AccountHolderNameResponse getAccountHolderName(String accountNo) {
        AccountHolderNameResult account = bankingAccountQueryService.getAccountHolderName(accountNo);

        return AccountHolderNameResponse.builder()
                .bankCode(account.bankCode())
                .bankName(account.bankName())
                .accountNo(account.accountNo())
                .userName(account.userName())
                .currency(account.currency())
                .build();
    }

    public TransactionListResponse getTransactions(Long accountId, LocalDate startDate, LocalDate endDate, String type, int page, int size) {
        getActiveAccount(accountId);

        Pageable pageable = PageRequest.of(page, size);
        TransactionType transactionType = parseTransactionType(type);
        LocalDateTime startDateTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay();

        Page<Transaction> result = transactionRepository.search(
                accountId,
                resolveCurrentUserId(),
                transactionType,
                startDateTime,
                endDateTime,
                pageable
        );

        List<TransactionSummaryResponse> transactions = result.getContent().stream()
                .map(this::toTransactionSummaryResponse)
                .toList();

        return TransactionListResponse.builder()
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .isLast(result.isLast())
                .transactions(transactions)
                .build();
    }

    public TransactionDetailResponse getTransactionDetail(Long accountId, Long transactionId) {
        getActiveAccount(accountId);

        Transaction transaction = transactionRepository.findByIdAndAccountIdAndAccountUserId(
                        transactionId,
                        accountId,
                        resolveCurrentUserId()
                )
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));

        return toTransactionDetailResponse(transaction);
    }

    private Account getActiveAccount(Long accountId) {
        return accountRepository.findByIdAndUserIdAndStatus(accountId, resolveCurrentUserId(), AccountStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private AccountSummaryResponse toAccountSummaryResponse(DemandDepositAccountResult account) {
        return AccountSummaryResponse.builder()
                .bankCode(account.bankCode())
                .bankName(account.bankName())
                .userName(account.userName())
                .accountNo(maskAccountNumber(account.accountNo()))
                .accountName(account.accountName())
                .accountTypeCode(account.accountTypeCode())
                .accountTypeName(account.accountTypeName())
                .accountCreatedDate(account.accountCreatedDate())
                .accountExpiryDate(account.accountExpiryDate())
                .dailyTransferLimit(account.dailyTransferLimit())
                .oneTimeTransferLimit(account.oneTimeTransferLimit())
                .accountBalance(account.accountBalance())
                .lastTransactionDate(account.lastTransactionDate())
                .currency(account.currency())
                .build();
    }

    private TransactionSummaryResponse toTransactionSummaryResponse(Transaction transaction) {
        return TransactionSummaryResponse.builder()
                .transactionId(transaction.getId())
                .transactionDate(transaction.getTransactionDate())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .counterpartyName(transaction.getCounterpartyName())
                .category(transaction.getCategory())
                .build();
    }

    private TransactionDetailResponse toTransactionDetailResponse(Transaction transaction) {
        return TransactionDetailResponse.builder()
                .transactionId(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .transactionDate(transaction.getTransactionDate())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .counterpartyName(transaction.getCounterpartyName())
                .counterpartyBank(transaction.getCounterpartyBank())
                .counterpartyAccount(transaction.getCounterpartyAccount())
                .fee(transaction.getFee())
                .category(transaction.getCategory())
                .memo(transaction.getMemo())
                .build();
    }

    private TransactionType parseTransactionType(String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return null;
        }

        try {
            return TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_TRANSACTION_TYPE);
        }
    }

    private long toLongBalance(DemandDepositAccountResult account) {
        if (account.accountBalance() == null || account.accountBalance().isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(account.accountBalance());
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 6) {
            return accountNumber;
        }

        return accountNumber.substring(0, 3) + "-***-" + accountNumber.substring(accountNumber.length() - 6);
    }

    private Long resolveCurrentUserId() {
        // TODO: Replace with authenticated user resolution when auth is implemented.
        return DEMO_USER_ID;
    }
}
