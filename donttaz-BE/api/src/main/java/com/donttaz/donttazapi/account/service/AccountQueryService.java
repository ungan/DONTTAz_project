package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.response.AccountBalanceResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountHolderNameResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.AccountSummaryResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionSummaryResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;
    private final DemandDepositService demandDepositService;
    private final AccountSyncService accountSyncService;
    private final BankingAccountQueryService bankingAccountQueryService;
    private final CurrentUserProvider currentUserProvider;

    public AccountQueryService(
            AccountRepository accountRepository,
            DemandDepositService demandDepositService,
            AccountSyncService accountSyncService,
            BankingAccountQueryService bankingAccountQueryService,
            CurrentUserProvider currentUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.demandDepositService = demandDepositService;
        this.accountSyncService = accountSyncService;
        this.bankingAccountQueryService = bankingAccountQueryService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public AccountListResponse getAccounts() {
        List<DemandDepositAccountResult> accounts = demandDepositService.getAccounts();
        accountSyncService.syncDemandDepositAccounts(accounts);
        Map<String, Account> accountMetadata = accountRepository
                .findAllByUserIdAndStatusAndIsDeletedFalse(resolveCurrentUserId(), AccountStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(this::accountKey, Function.identity(), (left, right) -> left));

        List<AccountSummaryResponse> accountList = accounts.stream()
                .map(account -> toAccountSummaryResponse(account, accountMetadata.get(accountKey(account.bankCode(), account.accountNo()))))
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
        Account account = getActiveAccount(accountId);
        TransactionQueryResult result = bankingAccountQueryService.getTransactions(
                account.getAccountNumber(),
                startDate,
                endDate,
                toExternalTransactionType(type),
                page,
                size
        );

        List<TransactionSummaryResponse> transactions = result.transactions().stream()
                .map(this::toTransactionSummaryResponse)
                .toList();

        return TransactionListResponse.builder()
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .isLast(result.last())
                .transactions(transactions)
                .build();
    }

    public TransactionDetailResponse getTransactionDetail(Long accountId, String transactionId) {
        Account account = getActiveAccount(accountId);
        TransactionDetailResult transaction = bankingAccountQueryService.getTransactionDetail(
                account.getAccountNumber(),
                transactionId
        );

        return toTransactionDetailResponse(accountId, transaction);
    }

    private Account getActiveAccount(Long accountId) {
        return accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(
                        accountId,
                        resolveCurrentUserId(),
                        AccountStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private AccountSummaryResponse toAccountSummaryResponse(DemandDepositAccountResult account, Account metadata) {
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
                .isPrimary(metadata != null && Boolean.TRUE.equals(metadata.getIsPrimary()))
                .build();
    }

    private String accountKey(Account account) {
        return accountKey(account.getBankCode(), account.getAccountNumber());
    }

    private String accountKey(String bankCode, String accountNumber) {
        return bankCode + ":" + accountNumber;
    }

    private TransactionSummaryResponse toTransactionSummaryResponse(TransactionSummaryResult transaction) {
        return TransactionSummaryResponse.builder()
                .transactionId(transaction.externalTransactionId())
                .transactionDate(transaction.transactionDate())
                .type(transaction.type())
                .amount(transaction.amount())
                .balanceAfter(transaction.balanceAfter())
                .counterpartyName(transaction.counterpartyName())
                .category(transaction.category())
                .build();
    }

    private TransactionDetailResponse toTransactionDetailResponse(Long accountId, TransactionDetailResult transaction) {
        return TransactionDetailResponse.builder()
                .transactionId(transaction.externalTransactionId())
                .accountId(accountId)
                .transactionDate(transaction.transactionDate())
                .type(transaction.type())
                .amount(transaction.amount())
                .balanceAfter(transaction.balanceAfter())
                .counterpartyName(transaction.counterpartyName())
                .counterpartyBank(transaction.counterpartyBank())
                .counterpartyAccount(transaction.counterpartyAccount())
                .fee(transaction.fee())
                .category(transaction.category())
                .memo(transaction.memo())
                .build();
    }

    private String toExternalTransactionType(String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return "A";
        }

        return switch (type.toUpperCase()) {
            case "A", "ALL" -> "A";
            case "M", "DEPOSIT" -> "M";
            case "D", "WITHDRAWAL" -> "D";
            default -> throw new CustomException(ErrorCode.INVALID_TRANSACTION_TYPE);
        };
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
        return currentUserProvider.getCurrentUserId();
    }
}
