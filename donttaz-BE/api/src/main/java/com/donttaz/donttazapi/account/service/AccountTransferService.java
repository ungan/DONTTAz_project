package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.AccountVerification;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.domain.TransactionType;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationConfirmRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferItemResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationConfirmResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationRequestResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.AccountVerificationRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferLimitCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
import com.donttaz.donttazapi.notification.service.PushNotificationService;
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
    private static final String TRANSACTION_SOURCE_FIN_API = "FIN_API";
    private static final String SELF_COUNTERPARTY_NAME = "본인";
    private static final String TRANSFER_COUNTERPARTY_NAME = "이체";
    private static final String PAYMENT_SETTLEMENT_SUMMARY = "PAYMENT_SETTLEMENT";
    private static final String DEPOSIT_LABEL = "입금";
    private static final String WITHDRAWAL_LABEL = "출금";
    private static final String DEPOSIT_TRANSFER_LABEL = "이체 입금";
    private static final String WITHDRAWAL_TRANSFER_LABEL = "이체 출금";

    private final BankingClient bankingClient;
    private final AccountSyncService accountSyncService;
    private final AccountRepository accountRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserProvider currentUserProvider;
    private final FinApiProperties finApiProperties;
    private final PushNotificationService pushNotificationService;

    public AccountTransferService(
            BankingClient bankingClient,
            AccountSyncService accountSyncService,
            AccountRepository accountRepository,
            AccountVerificationRepository accountVerificationRepository,
            TransactionRepository transactionRepository,
            CurrentUserProvider currentUserProvider,
            FinApiProperties finApiProperties,
            PushNotificationService pushNotificationService) {
        this.bankingClient = bankingClient;
        this.accountSyncService = accountSyncService;
        this.accountRepository = accountRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserProvider = currentUserProvider;
        this.finApiProperties = finApiProperties;
        this.pushNotificationService = pushNotificationService;
    }

    public OneWonVerificationRequestResponse requestOneWonVerification(OneWonVerificationRequest request) {
        Long userId = resolveCurrentUserId();
        String authText = buildAuthText();
        OneWonVerificationRequestResult result = bankingClient.requestOneWonVerification(
                userId,
                new OneWonVerificationRequestCommand(request.accountNo(), authText));

        accountVerificationRepository.save(
                AccountVerification.builder()
                        .verificationId(result.transactionUniqueNo())
                        .userId(userId)
                        .bankCode(finApiProperties.getInstitutionCode())
                        .accountNumber(result.accountNo())
                        .authCode(null)
                        .expiredAt(LocalDateTime.now().plusSeconds(finApiProperties.getOneWonExpirationSeconds()))
                        .verified(false)
                        .consentToken(null)
                        .build());

        return OneWonVerificationRequestResponse.builder()
                .verificationId(result.transactionUniqueNo())
                .accountNo(result.accountNo())
                .build();
    }

    public OneWonVerificationConfirmResponse confirmOneWonVerification(OneWonVerificationConfirmRequest request) {
        Long userId = resolveCurrentUserId();
        AccountVerification verification = accountVerificationRepository
                .findByVerificationIdAndUserId(request.verificationId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND));

        if (verification.isExpired(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.ACCOUNT_VERIFICATION_EXPIRED);
        }

        OneWonVerificationConfirmResult result = bankingClient.confirmOneWonVerification(
                userId,
                new OneWonVerificationConfirmCommand(
                        verification.getAccountNumber(),
                        buildAuthText(),
                        request.authCode()));

        if (!result.verified()) {
            throw new CustomException(ErrorCode.ACCOUNT_VERIFICATION_FAILED);
        }

        verification.markVerified(request.authCode());

        return OneWonVerificationConfirmResponse.builder()
                .verificationId(result.transactionUniqueNo())
                .accountNo(result.accountNo())
                .status(result.status())
                .verified(true)
                .build();
    }

    public AccountDepositResponse deposit(String accountNo, AccountDepositRequest request) {
        log.info(
                "deposit requested accountNo={} amount={} summary={}",
                accountNo,
                request.transactionBalance(),
                request.transactionSummary());
        Long userId = resolveUserIdForAccountOperation(accountNo);
        log.info("deposit resolved userId={} accountNo={}", userId, accountNo);
        return deposit(userId, accountNo, request);
    }

    public AccountDepositResponse deposit(Long userId, String accountNo, AccountDepositRequest request) {
        try {
            log.info(
                    "deposit calling banking client userId={} accountNo={} amount={} summary={}",
                    userId,
                    accountNo,
                    request.transactionBalance(),
                    request.transactionSummary());
            AccountDepositResult result = bankingClient.deposit(
                    userId,
                    new AccountDepositCommand(accountNo, request.transactionBalance(), request.transactionSummary())
            );
            persistTransaction(
                    userId,
                    accountNo,
                    result.transactionUniqueNo(),
                    TransactionType.DEPOSIT,
                    request.transactionBalance(),
                    request.transactionSummary(),
                    result.transactionDate());
            log.info(
                    "deposit completed userId={} accountNo={} transactionUniqueNo={} transactionDate={}",
                    userId,
                    accountNo,
                    result.transactionUniqueNo(),
                    result.transactionDate());
            sendDepositNotification(userId, request);

            return AccountDepositResponse.builder()
                    .transactionUniqueNo(result.transactionUniqueNo())
                    .transactionDate(result.transactionDate())
                    .build();
        } catch (RuntimeException exception) {
            log.error(
                    "deposit failed userId={} accountNo={} amount={} summary={} message={}",
                    userId,
                    accountNo,
                    request.transactionBalance(),
                    request.transactionSummary(),
                    exception.getMessage(),
                    exception);
            throw exception;
        }
    }

    private void sendDepositNotification(Long userId, AccountDepositRequest request) {
        try {
            pushNotificationService.sendDepositNotification(
                    userId,
                    toLong(request.transactionBalance()),
                    request.transactionSummary()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "deposit notification failed userId={} amount={} summary={} message={}",
                    userId,
                    request.transactionBalance(),
                    request.transactionSummary(),
                    exception.getMessage()
            );
        }
    }

    public AccountTransferResponse transfer(AccountTransferRequest request) {
        return transfer(resolveUserIdForAccountOperation(request.withdrawalAccountNo()), request);
    }

    public AccountTransferResponse transfer(Long userId, AccountTransferRequest request) {
        TransferResult result = bankingClient.transfer(
                userId,
                new TransferCommand(
                        request.depositAccountNo(),
                        request.depositTransactionSummary(),
                        request.transactionBalance(),
                        request.withdrawalAccountNo(),
                        request.withdrawalTransactionSummary()
                )
        );
        persistTransferTransactions(userId, request, result);
        sendTransferNotification(userId, request);

        return AccountTransferResponse.builder()
                .transactions(
                        result.transactions().stream()
                                .map(this::toAccountTransferItemResponse)
                                .toList())
                .build();
    }

    private void sendTransferNotification(Long userId, AccountTransferRequest request) {
        try {
            Long amount = toLong(request.transactionBalance());
            if (isPaymentTransfer(request)) {
                pushNotificationService.sendPaymentNotification(userId, amount, request.withdrawalTransactionSummary());
                return;
            }

            pushNotificationService.sendTransferNotification(userId, amount, request.withdrawalTransactionSummary());
        } catch (RuntimeException exception) {
            log.warn(
                    "transfer notification failed userId={} amount={} withdrawalSummary={} depositSummary={} message={}",
                    userId,
                    request.transactionBalance(),
                    request.withdrawalTransactionSummary(),
                    request.depositTransactionSummary(),
                    exception.getMessage()
            );
        }
    }

    private boolean isPaymentTransfer(AccountTransferRequest request) {
        return PAYMENT_SETTLEMENT_SUMMARY.equals(request.depositTransactionSummary());
    }

    public AccountTransferLimitUpdateResponse updateTransferLimit(
            String accountNo,
            AccountTransferLimitUpdateRequest request) {
        AccountDetailResult result = bankingClient.updateTransferLimit(
                resolveCurrentUserId(),
                new TransferLimitCommand(
                        accountNo,
                        request.oneTimeTransferLimit(),
                        request.dailyTransferLimit()));
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
        return withdrawal(resolveUserIdForAccountOperation(accountNo), accountNo, request);
    }

    public AccountWithdrawalResponse withdrawal(Long userId, String accountNo, AccountWithdrawalRequest request) {
        AccountWithdrawalResult result = bankingClient.withdrawal(
                userId,
                new AccountWithdrawalCommand(accountNo, request.transactionBalance(), request.transactionSummary())
        );
        persistTransaction(
                userId,
                accountNo,
                result.transactionUniqueNo(),
                TransactionType.WITHDRAWAL,
                request.transactionBalance(),
                request.transactionSummary(),
                result.transactionDate());
        sendWithdrawalNotification(userId, request);

        return AccountWithdrawalResponse.builder()
                .transactionUniqueNo(result.transactionUniqueNo())
                .transactionDate(result.transactionDate())
                .build();
    }

    private void sendWithdrawalNotification(Long userId, AccountWithdrawalRequest request) {
        try {
            pushNotificationService.sendWithdrawalNotification(
                    userId,
                    toLong(request.transactionBalance()),
                    request.transactionSummary()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "withdrawal notification failed userId={} amount={} summary={} message={}",
                    userId,
                    request.transactionBalance(),
                    request.transactionSummary(),
                    exception.getMessage()
            );
        }
    }

    private void persistTransaction(
            Long userId,
            String accountNo,
            String externalTransactionId,
            TransactionType type,
            String amount,
            String summary,
            String transactionDate
    ) {
        Account account = accountSyncService.syncAccountSnapshot(userId, accountNo);
        transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .userId(account.getUserId())
                        .source(TRANSACTION_SOURCE_FIN_API)
                        .sourceTxId(externalTransactionId)
                        .txType(toSourceTransactionType(type))
                        .txTypeName(toTransactionTypeLabel(type))
                        .amount(toLong(amount))
                        .balanceSnapshot(toLong(account.getBalance()))
                        .counterpartyName(SELF_COUNTERPARTY_NAME)
                        .transactionSummary(summary)
                        .transactionAt(toTransactionAt(transactionDate))
                        .syncedAt(LocalDateTime.now())
                        .build());
    }

    private void persistTransferTransactions(Long userId, AccountTransferRequest request, TransferResult result) {
        try {
            Account withdrawalAccount = accountSyncService.syncAccountSnapshot(userId, request.withdrawalAccountNo());
            saveTransferTransaction(
                    withdrawalAccount,
                    findTransactionByAccountNo(result.transactions(), request.withdrawalAccountNo()).orElse(null),
                    TransactionType.WITHDRAWAL,
                    request.transactionBalance(),
                    request.withdrawalTransactionSummary(),
                    request.depositAccountNo(),
                    resolveTransferCounterpartyName(TransactionType.WITHDRAWAL, request)
            );

            syncSystemDepositAccount(request.depositAccountNo())
                    .ifPresent(account -> saveTransferTransaction(
                            account,
                            findTransactionByAccountNo(result.transactions(), request.depositAccountNo()).orElse(null),
                            TransactionType.DEPOSIT,
                            request.transactionBalance(),
                            request.depositTransactionSummary(),
                            request.withdrawalAccountNo(),
                            resolveTransferCounterpartyName(TransactionType.DEPOSIT, request)
                    ));
        } catch (RuntimeException exception) {
            log.warn(
                    "transfer persistence failed withdrawalAccountNo={} depositAccountNo={} message={}",
                    request.withdrawalAccountNo(),
                    request.depositAccountNo(),
                    exception.getMessage());
        }
    }

    private Optional<Account> syncSystemDepositAccount(String accountNo) {
        Optional<Account> depositAccount = accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse(accountNo, AccountStatus.ACTIVE);
        if (depositAccount.isEmpty()) {
            return Optional.empty();
        }

        try {
            Long ownerUserId = depositAccount.get().getUserId();
            return Optional.of(accountSyncService.syncAccountSnapshot(ownerUserId, accountNo));
        } catch (RuntimeException exception) {
            log.warn("deposit account snapshot sync skipped accountNo={} message={}", accountNo,
                    exception.getMessage());
            return Optional.empty();
        }
    }

    private void saveTransferTransaction(
            Account account,
            TransferTransactionResult transactionResult,
            TransactionType type,
            String amount,
            String summary,
            String counterpartyAccountNo,
            String counterpartyName
    ) {
        if (transactionResult == null) {
            log.warn("transfer transaction result missing accountNo={} direction={}", account.getAccountNumber(), type);
            return;
        }

        transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .userId(account.getUserId())
                        .source(TRANSACTION_SOURCE_FIN_API)
                        .sourceTxId(transactionResult.transactionUniqueNo())
                        .txType(resolveSourceTransactionType(type, transactionResult.transactionType()))
                        .txTypeName(resolveSourceTransactionTypeName(type, transactionResult.transactionTypeName()))
                        .amount(toLong(amount))
                        .balanceSnapshot(toLong(account.getBalance()))
                        .counterpartyName(counterpartyName)
                        .counterpartyAccountNo(counterpartyAccountNo)
                        .transactionSummary(summary)
                        .transactionAt(toTransactionAt(transactionResult.transactionDate()))
                        .syncedAt(LocalDateTime.now())
                        .build());
    }

    private String resolveTransferCounterpartyName(TransactionType type, AccountTransferRequest request) {
        if (type == TransactionType.WITHDRAWAL
                && PAYMENT_SETTLEMENT_SUMMARY.equals(request.depositTransactionSummary())
                && request.withdrawalTransactionSummary() != null
                && !request.withdrawalTransactionSummary().isBlank()) {
            return request.withdrawalTransactionSummary();
        }
        return TRANSFER_COUNTERPARTY_NAME;
    }

    private Optional<TransferTransactionResult> findTransactionByAccountNo(List<TransferTransactionResult> transactions, String accountNo) {
        return transactions.stream()
                .filter(transaction -> accountNo.equals(transaction.accountNo()))
                .findFirst();
    }

    private String buildAuthText() {
        String prefix = finApiProperties.getOneWonAuthText() == null ? "" : finApiProperties.getOneWonAuthText().trim();
        if (prefix.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "one-won auth text is not configured.");
        }

        if (prefix.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "one-won auth text must be 20 characters or fewer.");
        }
        return prefix;
    }

    private Long resolveCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private Long resolveUserIdForAccountOperation(String accountNo) {
        try {
            return resolveCurrentUserId();
        } catch (CustomException exception) {
            if (exception.getErrorCode() != ErrorCode.AUTHENTICATION_REQUIRED) {
                throw exception;
            }

            return accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse(accountNo, AccountStatus.ACTIVE)
                    .map(Account::getUserId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        }
    }

    private Long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Transaction amount is invalid.");
        }
    }

    private Long toLong(Long value) {
        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "Account balance is invalid.");
        }
        return value;
    }

    private LocalDateTime toTransactionAt(String transactionDate) {
        LocalDateTime now = LocalDateTime.now();
        if (transactionDate == null || transactionDate.isBlank()) {
            return now;
        }

        try {
            return LocalDate.parse(transactionDate, DateTimeFormatter.BASIC_ISO_DATE).atTime(now.toLocalTime());
        } catch (DateTimeParseException exception) {
            return now;
        }
    }

    private String toSourceTransactionType(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> "1";
            case WITHDRAWAL -> "2";
        };
    }

    private String resolveSourceTransactionType(TransactionType type, String txType) {
        if (txType == null || txType.isBlank()) {
            return toSourceTransactionType(type);
        }
        return txType;
    }

    private String resolveSourceTransactionTypeName(TransactionType type, String txTypeName) {
        if (txTypeName == null || txTypeName.isBlank()) {
            return toTransactionTypeLabel(type);
        }
        return switch (txTypeName.toUpperCase()) {
            case "DEPOSIT", "1" -> DEPOSIT_LABEL;
            case "WITHDRAWAL", "2" -> WITHDRAWAL_LABEL;
            case "DEPOSIT_TRANSFER" -> DEPOSIT_TRANSFER_LABEL;
            case "WITHDRAWAL_TRANSFER" -> WITHDRAWAL_TRANSFER_LABEL;
            default -> txTypeName;
        };
    }

    private String toTransactionTypeLabel(TransactionType type) {
        return switch (type) {
            case DEPOSIT -> DEPOSIT_LABEL;
            case WITHDRAWAL -> WITHDRAWAL_LABEL;
        };
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
