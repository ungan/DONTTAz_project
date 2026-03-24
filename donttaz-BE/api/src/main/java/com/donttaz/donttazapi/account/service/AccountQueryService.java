package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
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
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.payment.domain.Payment;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AccountQueryService {

    private static final String PAYMENT_CATEGORY = "PAYMENT";
    private static final int DEFAULT_TRANSACTION_LOOKBACK_MONTHS = 3;
    private static final String SELF_COUNTERPARTY_NAME = "SELF";
    private static final String TRANSFER_COUNTERPARTY_NAME = "TRANSFER";
    private static final String DEPOSIT_TYPE = "DEPOSIT";
    private static final String WITHDRAWAL_TYPE = "WITHDRAWAL";
    private static final String DEPOSIT_TRANSFER_TYPE = "DEPOSIT_TRANSFER";
    private static final String WITHDRAWAL_TRANSFER_TYPE = "WITHDRAWAL_TRANSFER";
    private static final String SELF_COUNTERPARTY_LABEL = "본인";
    private static final String TRANSFER_COUNTERPARTY_LABEL = "이체";
    private static final String DEPOSIT_LABEL = "입금";
    private static final String WITHDRAWAL_LABEL = "출금";
    private static final String DEPOSIT_TRANSFER_LABEL = "이체 입금";
    private static final String WITHDRAWAL_TRANSFER_LABEL = "이체 출금";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAiMetadataRepository transactionAiMetadataRepository;
    private final DemandDepositService demandDepositService;
    private final AccountSyncService accountSyncService;
    private final BankingAccountQueryService bankingAccountQueryService;
    private final TransactionSyncService transactionSyncService;
    private final PaymentRepository paymentRepository;
    private final CurrentUserProvider currentUserProvider;

    public AccountQueryService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransactionAiMetadataRepository transactionAiMetadataRepository,
            DemandDepositService demandDepositService,
            AccountSyncService accountSyncService,
            BankingAccountQueryService bankingAccountQueryService,
            TransactionSyncService transactionSyncService,
            PaymentRepository paymentRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionAiMetadataRepository = transactionAiMetadataRepository;
        this.demandDepositService = demandDepositService;
        this.accountSyncService = accountSyncService;
        this.bankingAccountQueryService = bankingAccountQueryService;
        this.transactionSyncService = transactionSyncService;
        this.paymentRepository = paymentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public AccountListResponse getAccounts() {
        Long userId = resolveCurrentUserId();
        List<DemandDepositAccountResult> accounts = demandDepositService.getAccounts();
        accountSyncService.syncDemandDepositAccounts(accounts);
        Map<String, Account> accountMetadata = accountRepository
                .findAllByUserIdAndStatusAndIsDeletedFalse(userId, AccountStatus.ACTIVE)
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

    @Transactional
    public TransactionListResponse getTransactions(Long accountId, LocalDate startDate, LocalDate endDate, String type, int page, int size) {
        Long userId = resolveCurrentUserId();
        Account account = getActiveAccount(accountId, userId);
        LocalDate resolvedEndDate = endDate != null ? endDate : LocalDate.now();
        LocalDate resolvedStartDate = startDate != null ? startDate : resolvedEndDate.minusMonths(DEFAULT_TRANSACTION_LOOKBACK_MONTHS);
        validateDateRange(resolvedStartDate, resolvedEndDate);

        transactionSyncService.syncTransactions(account, resolvedStartDate, resolvedEndDate);

        Page<Transaction> transactionPage = searchTransactions(
                accountId,
                userId,
                resolvedStartDate,
                resolvedEndDate,
                type,
                page,
                size
        );
        Map<String, Payment> paymentByTransactionId = mapPaymentsByTransactionId(userId, transactionPage.getContent());
        Map<Long, TransactionAiMetadata> metadataByTransactionId = mapAiMetadataByTransactionId(transactionPage.getContent());

        List<TransactionSummaryResponse> transactions = transactionPage.getContent().stream()
                .map(transaction -> toTransactionSummaryResponse(
                        transaction,
                        paymentByTransactionId.get(transaction.getSourceTxId()),
                        metadataByTransactionId.get(transaction.getId())
                ))
                .toList();

        return TransactionListResponse.builder()
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .isLast(transactionPage.isLast())
                .transactions(transactions)
                .build();
    }

    @Transactional
    public TransactionDetailResponse getTransactionDetail(Long accountId, String transactionId) {
        Long userId = resolveCurrentUserId();
        Account account = getActiveAccount(accountId, userId);
        Transaction transaction = findStoredTransaction(accountId, userId, transactionId)
                .orElseGet(() -> {
                    transactionSyncService.syncTransactions(
                            account,
                            LocalDate.now().minusMonths(DEFAULT_TRANSACTION_LOOKBACK_MONTHS),
                            LocalDate.now()
                    );
                    return findStoredTransaction(accountId, userId, transactionId)
                            .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
                });

        Payment payment = paymentRepository.findByUserIdAndWithdrawalTransactionId(userId, transaction.getSourceTxId())
                .orElse(null);
        TransactionAiMetadata metadata = transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                .orElse(null);

        return toTransactionDetailResponse(accountId, transaction, payment, metadata);
    }

    private Page<Transaction> searchTransactions(
            Long accountId,
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            String type,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
        List<String> txTypes = toDatabaseTransactionTypes(type);

        if (txTypes == null) {
            return transactionRepository.search(accountId, userId, startDateTime, endDateTime, pageable);
        }

        return transactionRepository.searchByTxTypes(accountId, userId, txTypes, startDateTime, endDateTime, pageable);
    }

    private Optional<Transaction> findStoredTransaction(Long accountId, Long userId, String transactionId) {
        Optional<Transaction> localTransaction = parseTransactionId(transactionId)
                .flatMap(id -> transactionRepository.findByIdAndAccountAccountIdAndAccountUserId(id, accountId, userId));

        if (localTransaction.isPresent()) {
            return localTransaction;
        }

        return transactionRepository.findByAccountAccountIdAndAccountUserIdAndSourceTxId(accountId, userId, transactionId);
    }

    private Account getActiveAccount(Long accountId, Long userId) {
        return accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(
                        accountId,
                        userId,
                        AccountStatus.ACTIVE
                )
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private AccountSummaryResponse toAccountSummaryResponse(DemandDepositAccountResult account, Account metadata) {
        return AccountSummaryResponse.builder()
                .accountId(metadata != null ? metadata.getAccountId() : null)
                .bankCode(account.bankCode())
                .bankName(account.bankName())
                .userName(account.userName())
                .accountNo(account.accountNo())
                .maskedAccountNo(maskAccountNumber(account.accountNo()))
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

    private Map<String, Payment> mapPaymentsByTransactionId(Long userId, List<Transaction> transactions) {
        List<String> transactionIds = transactions.stream()
                .map(Transaction::getSourceTxId)
                .filter(transactionId -> transactionId != null && !transactionId.isBlank())
                .toList();

        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        return paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(userId, transactionIds).stream()
                .collect(Collectors.toMap(Payment::getWithdrawalTransactionId, Function.identity(), (left, right) -> left));
    }

    private Map<Long, TransactionAiMetadata> mapAiMetadataByTransactionId(List<Transaction> transactions) {
        List<Long> transactionIds = transactions.stream()
                .map(Transaction::getId)
                .toList();

        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        return transactionAiMetadataRepository.findAllByTransactionIdIn(transactionIds).stream()
                .collect(Collectors.toMap(metadata -> metadata.getTransaction().getId(), Function.identity(), (left, right) -> left));
    }

    private TransactionSummaryResponse toTransactionSummaryResponse(
            Transaction transaction,
            Payment payment,
            TransactionAiMetadata metadata
    ) {
        return TransactionSummaryResponse.builder()
                .transactionId(transaction.getSourceTxId())
                .transactionDate(transaction.getTransactionAt())
                .type(toApiTransactionType(transaction.getTxType()))
                .amount(toInteger(transaction.getAmount()))
                .balanceAfter(toInteger(transaction.getBalanceSnapshot()))
                .counterpartyName(resolveCounterpartyName(transaction, payment))
                .category(resolveCategory(transaction, payment, metadata))
                .build();
    }

    private TransactionDetailResponse toTransactionDetailResponse(
            Long accountId,
            Transaction transaction,
            Payment payment,
            TransactionAiMetadata metadata
    ) {
        return TransactionDetailResponse.builder()
                .transactionId(transaction.getSourceTxId())
                .accountId(accountId)
                .transactionDate(transaction.getTransactionAt())
                .type(toApiTransactionType(transaction.getTxType()))
                .amount(toInteger(transaction.getAmount()))
                .balanceAfter(toInteger(transaction.getBalanceSnapshot()))
                .counterpartyName(resolveCounterpartyName(transaction, payment))
                .counterpartyBank(transaction.getCounterpartyBank())
                .counterpartyAccount(resolveCounterpartyAccount(transaction, payment))
                .fee(0)
                .category(resolveCategory(transaction, payment, metadata))
                .memo(resolveMemo(transaction, payment))
                .build();
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "startDate must be before or equal to endDate.");
        }
    }

    private List<String> toDatabaseTransactionTypes(String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type) || "A".equalsIgnoreCase(type)) {
            return null;
        }

        return switch (type.toUpperCase()) {
            case "M", "DEPOSIT" -> List.of("DEPOSIT", "1");
            case "D", "WITHDRAWAL" -> List.of("WITHDRAWAL", "2");
            default -> throw new CustomException(ErrorCode.INVALID_TRANSACTION_TYPE);
        };
    }

    private String toApiTransactionType(String type) {
        if (type == null || type.isBlank()) {
            return "UNKNOWN";
        }

        return switch (type.toUpperCase()) {
            case "1", "DEPOSIT" -> "DEPOSIT";
            case "2", "WITHDRAWAL" -> "WITHDRAWAL";
            default -> type;
        };
    }

    private String resolveCounterpartyName(Transaction transaction, Payment payment) {
        if (payment != null && payment.getMerchantName() != null && !payment.getMerchantName().isBlank()) {
            return payment.getMerchantName();
        }
        return translateCounterpartyName(transaction.getCounterpartyName());
    }

    private String resolveCounterpartyAccount(Transaction transaction, Payment payment) {
        if (payment != null && payment.getSettlementAccountNo() != null && !payment.getSettlementAccountNo().isBlank()) {
            return payment.getSettlementAccountNo();
        }
        return transaction.getCounterpartyAccountNo();
    }

    private String resolveMemo(Transaction transaction, Payment payment) {
        if (payment != null && payment.getMerchantName() != null && !payment.getMerchantName().isBlank()) {
            return payment.getMerchantName();
        }
        return translateDisplayText(transaction.getTransactionSummary());
    }

    private String resolveCategory(Transaction transaction, Payment payment, TransactionAiMetadata metadata) {
        if (payment != null) {
            return PAYMENT_CATEGORY;
        }

        if (metadata != null) {
            if (Boolean.TRUE.equals(metadata.getIsModified())
                    && metadata.getModifiedCategory() != null
                    && !metadata.getModifiedCategory().isBlank()) {
                return metadata.getModifiedCategory();
            }

            if (metadata.getAiCategory() != null && !metadata.getAiCategory().isBlank()) {
                return metadata.getAiCategory();
            }
        }

        if (transaction.getAiCategory() != null && !transaction.getAiCategory().isBlank()) {
            return transaction.getAiCategory();
        }

        if (transaction.getTxTypeName() != null && !transaction.getTxTypeName().isBlank()) {
            return translateTransactionLabel(transaction.getTxTypeName());
        }

        return translateTransactionLabel(toApiTransactionType(transaction.getTxType()));
    }

    private Optional<Long> parseTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(transactionId));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private int toInteger(Long value) {
        if (value == null) {
            return 0;
        }

        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException exception) {
            return 0;
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
        return currentUserProvider.getCurrentUserId();
    }

    private String translateCounterpartyName(String counterpartyName) {
        if (counterpartyName == null || counterpartyName.isBlank()) {
            return counterpartyName;
        }

        return switch (counterpartyName.toUpperCase()) {
            case SELF_COUNTERPARTY_NAME -> SELF_COUNTERPARTY_LABEL;
            case TRANSFER_COUNTERPARTY_NAME -> TRANSFER_COUNTERPARTY_LABEL;
            default -> counterpartyName;
        };
    }

    private String translateTransactionLabel(String label) {
        if (label == null || label.isBlank()) {
            return label;
        }

        return switch (label.toUpperCase()) {
            case DEPOSIT_TYPE, "1" -> DEPOSIT_LABEL;
            case WITHDRAWAL_TYPE, "2" -> WITHDRAWAL_LABEL;
            case DEPOSIT_TRANSFER_TYPE -> DEPOSIT_TRANSFER_LABEL;
            case WITHDRAWAL_TRANSFER_TYPE -> WITHDRAWAL_TRANSFER_LABEL;
            default -> label;
        };
    }

    private String translateDisplayText(String value) {
        return translateTransactionLabel(translateCounterpartyName(value));
    }
}
