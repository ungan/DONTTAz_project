package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class TransactionSyncService {

    private static final String TRANSACTION_SOURCE_FIN_API = "FIN_API";
    private static final String ALL_TRANSACTION_TYPE = "A";

    private final BankingAccountQueryService bankingAccountQueryService;
    private final TransactionRepository transactionRepository;

    public TransactionSyncService(
            BankingAccountQueryService bankingAccountQueryService,
            TransactionRepository transactionRepository
    ) {
        this.bankingAccountQueryService = bankingAccountQueryService;
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> syncTransactions(Account account, LocalDate startDate, LocalDate endDate) {
        TransactionQueryResult queryResult = bankingAccountQueryService.getTransactions(
                account.getAccountNumber(),
                startDate,
                endDate,
                ALL_TRANSACTION_TYPE,
                0,
                0
        );

        LocalDateTime syncedAt = LocalDateTime.now();
        List<Transaction> newTransactions = new ArrayList<>();

        for (TransactionSummaryResult summary : queryResult.transactions()) {
            String sourceTransactionId = summary.externalTransactionId();
            if (sourceTransactionId == null || sourceTransactionId.isBlank()) {
                continue;
            }

            if (transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(
                    account.getAccountId(),
                    TRANSACTION_SOURCE_FIN_API,
                    sourceTransactionId
            )) {
                continue;
            }

            try {
                Transaction savedTransaction = transactionRepository.save(
                        Transaction.builder()
                                .account(account)
                                .userId(account.getUserId())
                                .source(TRANSACTION_SOURCE_FIN_API)
                                .sourceTxId(sourceTransactionId)
                                .txType(normalizeTransactionType(summary.type()))
                                .txTypeName(resolveTransactionTypeName(summary))
                                .amount(toLong(summary.amount()))
                                .balanceSnapshot(toLong(summary.balanceAfter()))
                                .counterpartyName(blankToNull(summary.counterpartyName()))
                                .transactionSummary(blankToNull(summary.counterpartyName()))
                                .transactionAt(resolveTransactionAt(summary, endDate))
                                .syncedAt(syncedAt)
                                .build()
                );
                newTransactions.add(savedTransaction);
            } catch (DataIntegrityViolationException exception) {
                log.debug(
                        "transaction sync skipped duplicate accountId={} sourceTxId={}",
                        account.getAccountId(),
                        sourceTransactionId
                );
            }
        }

        account.updateLastSyncedAt(syncedAt);
        return newTransactions;
    }

    private String normalizeTransactionType(String type) {
        if (type == null || type.isBlank()) {
            return "UNKNOWN";
        }

        return switch (type.toUpperCase()) {
            case "1", "DEPOSIT" -> "DEPOSIT";
            case "2", "WITHDRAWAL" -> "WITHDRAWAL";
            default -> type;
        };
    }

    private String resolveTransactionTypeName(TransactionSummaryResult summary) {
        if (summary.category() != null && !summary.category().isBlank()) {
            return summary.category();
        }
        return normalizeTransactionType(summary.type());
    }

    private LocalDateTime resolveTransactionAt(TransactionSummaryResult summary, LocalDate endDate) {
        if (summary.transactionDate() != null) {
            return summary.transactionDate();
        }

        if (endDate != null) {
            return endDate.atStartOfDay();
        }

        return LocalDateTime.now();
    }

    private Long toLong(Integer value) {
        return value == null ? 0L : value.longValue();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
