package com.donttaz.donttazapi.infra.banking.mapper;

import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountCloseResult;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
import com.donttaz.donttazapi.account.service.dto.AccountSummaryResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountBalanceResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDepositResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountHolderNameResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountWithdrawalResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountItemResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiCreateDemandDepositAccountRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiDeleteDemandDepositAccountResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiDemandDepositAccountRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiDemandDepositProductRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiOneWonConfirmResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiOneWonRequestResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionItemResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionListResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransferRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransferResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FinApiBankingMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    public DemandDepositProductResult toDemandDepositProductResult(FinApiDemandDepositProductRec response) {
        return new DemandDepositProductResult(
                response.accountTypeUniqueNo(),
                response.bankCode(),
                response.bankName(),
                response.accountTypeCode(),
                response.accountTypeName(),
                response.accountName(),
                response.accountDescription(),
                response.accountType()
        );
    }

    public List<DemandDepositProductResult> toDemandDepositProductResults(List<FinApiDemandDepositProductRec> responses) {
        return responses.stream()
                .map(this::toDemandDepositProductResult)
                .toList();
    }

    public DemandDepositAccountCreateResult toDemandDepositAccountCreateResult(FinApiCreateDemandDepositAccountRec response) {
        return new DemandDepositAccountCreateResult(
                response.bankCode(),
                response.accountNo(),
                response.currency().currency(),
                response.currency().currencyName()
        );
    }

    public DemandDepositAccountResult toDemandDepositAccountResult(FinApiDemandDepositAccountRec response) {
        return new DemandDepositAccountResult(
                response.bankCode(),
                response.bankName(),
                response.userName(),
                response.accountNo(),
                response.accountName(),
                response.accountTypeCode(),
                response.accountTypeName(),
                response.accountCreatedDate(),
                response.accountExpiryDate(),
                response.dailyTransferLimit(),
                response.oneTimeTransferLimit(),
                response.accountBalance(),
                response.lastTransactionDate(),
                response.currency()
        );
    }

    public List<DemandDepositAccountResult> toDemandDepositAccountResults(List<FinApiDemandDepositAccountRec> responses) {
        return responses.stream()
                .map(this::toDemandDepositAccountResult)
                .toList();
    }

    @SuppressWarnings("unused")
    public List<AccountSummaryResult> toAccountSummaryResults(List<FinApiAccountItemResponse> responses) {
        return responses.stream()
                .map(this::toAccountSummaryResult)
                .toList();
    }

    public AccountSummaryResult toAccountSummaryResult(FinApiAccountItemResponse response) {
        return new AccountSummaryResult(
                response.accountId(),
                response.bankCode(),
                response.bankName(),
                response.accountNumber(),
                response.accountAlias(),
                response.balance()
        );
    }

    public AccountDetailResult toAccountDetailResult(FinApiAccountDetailResponse response) {
        FinApiDemandDepositAccountRec rec = response.rec();
        return new AccountDetailResult(
                rec.bankCode(),
                rec.bankName(),
                rec.userName(),
                rec.accountNo(),
                rec.accountName(),
                rec.accountTypeCode(),
                rec.accountTypeName(),
                rec.accountCreatedDate(),
                rec.accountExpiryDate(),
                rec.dailyTransferLimit(),
                rec.oneTimeTransferLimit(),
                rec.accountBalance(),
                rec.lastTransactionDate(),
                rec.currency()
        );
    }

    public AccountBalanceResult toAccountBalanceResult(FinApiAccountBalanceResponse response) {
        return new AccountBalanceResult(
                response.rec().bankCode(),
                response.rec().accountNo(),
                response.rec().accountBalance(),
                response.rec().accountCreatedDate(),
                response.rec().accountExpiryDate(),
                response.rec().lastTransactionDate(),
                response.rec().currency()
        );
    }

    public AccountHolderNameResult toAccountHolderNameResult(FinApiAccountHolderNameResponse response) {
        return new AccountHolderNameResult(
                response.rec().bankCode(),
                response.rec().bankName(),
                response.rec().accountNo(),
                response.rec().userName(),
                response.rec().currency()
        );
    }

    public AccountDepositResult toAccountDepositResult(FinApiAccountDepositResponse response) {
        return new AccountDepositResult(
                response.rec().transactionUniqueNo(),
                response.rec().transactionDate()
        );
    }

    public AccountWithdrawalResult toAccountWithdrawalResult(FinApiAccountWithdrawalResponse response) {
        return new AccountWithdrawalResult(
                response.rec().transactionUniqueNo(),
                response.rec().transactionDate()
        );
    }

    public AccountCloseResult toAccountCloseResult(FinApiDeleteDemandDepositAccountResponse response) {
        return new AccountCloseResult(
                response.rec().status(),
                response.rec().accountNo(),
                response.rec().refundAccountNo(),
                response.rec().accountBalance()
        );
    }

    public TransactionQueryResult toTransactionQueryResult(FinApiTransactionListResponse response, int page, int size) {
        List<TransactionSummaryResult> transactions = defaultTransactionList(response).stream()
                .map(this::toTransactionSummaryResult)
                .toList();
        long totalElements = parseLong(response.rec() == null ? null : response.rec().totalCount());
        int safeSize = size <= 0 ? Math.max(transactions.size(), 1) : size;
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / safeSize);
        int fromIndex = Math.min(Math.max(page, 0) * safeSize, transactions.size());
        int toIndex = Math.min(fromIndex + safeSize, transactions.size());

        return new TransactionQueryResult(
                totalElements,
                totalPages,
                totalPages == 0 || page >= totalPages - 1,
                transactions.subList(fromIndex, toIndex)
        );
    }

    public TransactionSummaryResult toTransactionSummaryResult(FinApiTransactionItemResponse response) {
        return new TransactionSummaryResult(
                response.transactionUniqueNo(),
                toTransactionDate(response.transactionDate(), response.transactionTime()),
                toTransactionType(response.transactionType()),
                toInteger(response.transactionBalance()),
                toInteger(response.transactionAfterBalance()),
                blankToNull(response.transactionSummary()),
                response.transactionTypeName()
        );
    }

    public TransactionDetailResult toTransactionDetailResult(FinApiTransactionDetailResponse response) {
        FinApiTransactionItemResponse rec = response.rec();
        return new TransactionDetailResult(
                rec.transactionUniqueNo(),
                null,
                toTransactionDate(rec.transactionDate(), rec.transactionTime()),
                toTransactionType(rec.transactionType()),
                toInteger(rec.transactionBalance()),
                toInteger(rec.transactionAfterBalance()),
                blankToNull(rec.transactionSummary()),
                null,
                blankToNull(rec.transactionAccountNo()),
                0,
                rec.transactionTypeName(),
                blankToNull(rec.transactionMemo())
        );
    }

    public TransferResult toTransferResult(FinApiTransferResponse response) {
        return new TransferResult(
                response.rec().stream()
                        .map(this::toTransferTransactionResult)
                        .toList()
        );
    }

    public TransferTransactionResult toTransferTransactionResult(FinApiTransferRec response) {
        return new TransferTransactionResult(
                response.transactionUniqueNo(),
                response.accountNo(),
                response.transactionDate(),
                response.transactionType(),
                response.transactionTypeName(),
                response.transactionAccountNo()
        );
    }

    public OneWonVerificationRequestResult toOneWonVerificationRequestResult(FinApiOneWonRequestResponse response) {
        return new OneWonVerificationRequestResult(
                response.rec().transactionUniqueNo(),
                response.rec().accountNo()
        );
    }

    @SuppressWarnings("unused")
    public OneWonVerificationConfirmResult toOneWonVerificationConfirmResult(FinApiOneWonConfirmResponse response) {
        String status = response.rec() == null ? null : response.rec().status();
        return new OneWonVerificationConfirmResult(
                status,
                "SUCCESS".equalsIgnoreCase(status),
                response.rec() == null ? null : response.rec().transactionUniqueNo(),
                response.rec() == null ? null : response.rec().accountNo()
        );
    }

    private List<FinApiTransactionItemResponse> defaultTransactionList(FinApiTransactionListResponse response) {
        if (response.rec() == null || response.rec().list() == null) {
            return List.of();
        }
        return response.rec().list();
    }

    private LocalDateTime toTransactionDate(String transactionDate, String transactionTime) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(transactionDate, DATE_FORMATTER);
            LocalTime time = (transactionTime == null || transactionTime.isBlank())
                    ? LocalTime.now()
                    : LocalTime.parse(transactionTime, TIME_FORMATTER);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String toTransactionType(String transactionType) {
        return switch (transactionType) {
            case "1" -> "DEPOSIT";
            case "2" -> "WITHDRAWAL";
            default -> transactionType;
        };
    }

    private Integer toInteger(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            return Math.toIntExact(Long.parseLong(value));
        } catch (NumberFormatException | ArithmeticException exception) {
            return 0;
        }
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
