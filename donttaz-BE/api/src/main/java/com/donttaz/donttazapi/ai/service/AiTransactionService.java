package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionSyncResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.BankingAccountQueryService;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiTransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAiMetadataRepository transactionAiMetadataRepository;
    private final BankingAccountQueryService bankingAccountQueryService;
    private final AiServiceClient aiServiceClient;
    private final CurrentUserProvider currentUserProvider;

    public TransactionCategoryUpdateResponse updateTransactionCategory(Long transactionId, TransactionCategoryUpdateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.TRANSACTION_NOT_FOUND));
        
        if (!transaction.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        TransactionAiMetadata metadata = transactionAiMetadataRepository.findByTransactionId(transactionId)
                .orElseGet(() -> transactionAiMetadataRepository.save(
                        TransactionAiMetadata.builder()
                                .transaction(transaction)
                                .aiCategory("미분류")
                                .confidenceScore(0.0)
                                .build()
                ));

        String previousCategory = metadata.getAiCategory();
        metadata.updateCategory(request.updateCategory());

        return new TransactionCategoryUpdateResponse(
                transactionId,
                previousCategory,
                metadata.getModifiedCategory(),
                metadata.getIsModified(),
                metadata.getModifiedAt()
        );
    }

    public AiCategorizedListResponse getCategorizedTransactions(String yearMonth, Pageable pageable) {
        Long userId = currentUserProvider.getCurrentUserId();
        
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime startDate = ym.atDay(1).atStartOfDay();
        LocalDateTime endDate = ym.plusMonths(1).atDay(1).atStartOfDay();

        Page<Transaction> txPage = transactionRepository.searchWithAiMetadata(userId, startDate, endDate, pageable);

        List<AiCategorizedListResponse.AiCategorizedTransactionItem> items = txPage.getContent().stream()
                .map(tx -> {
                    TransactionAiMetadata metadata = transactionAiMetadataRepository.findByTransactionId(tx.getId())
                            .orElse(null);

                    return new AiCategorizedListResponse.AiCategorizedTransactionItem(
                            tx.getId(),
                            tx.getTransactionAt(),
                            tx.getCounterpartyName(),
                            tx.getAmount(),
                            metadata != null ? metadata.getAiCategory() : "미분류",
                            metadata != null ? metadata.getConfidenceScore() : 0.0,
                            metadata != null ? metadata.getIsModified() : false
                    );
                })
                .collect(Collectors.toList());

        return new AiCategorizedListResponse(txPage.getTotalElements(), items);
    }

    public AiTransactionSyncResponse syncTransactions(AiTransactionSyncRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        List<Account> accounts = accountRepository.findByUserId(userId);
        
        int totalSyncedCount = 0;
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now().minusMonths(3);
        LocalDate endDate = request.endDate() != null ? request.endDate() : LocalDate.now();

        for (Account account : accounts) {
            totalSyncedCount += syncAccountTransactions(account, startDate, endDate);
        }

        return new AiTransactionSyncResponse(totalSyncedCount, LocalDateTime.now());
    }

    private int syncAccountTransactions(Account account, LocalDate startDate, LocalDate endDate) {
        TransactionQueryResult queryResult = bankingAccountQueryService.getTransactions(
                account.getAccountNumber(),
                startDate,
                endDate,
                "ALL",
                1,
                100
        );

        List<Transaction> newTransactions = new ArrayList<>();

        for (TransactionSummaryResult res : queryResult.transactions()) {
            if (!transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(
                    account.getAccountId(), "FIN_API", res.externalTransactionId())) {
                
                Transaction transaction = Transaction.builder()
                        .account(account)
                        .userId(account.getUserId())
                        .source("FIN_API")
                        .sourceTxId(res.externalTransactionId())
                        .txType(res.type())
                        .txTypeName(res.type())
                        .amount(res.amount().longValue())
                        .balanceSnapshot(res.balanceAfter().longValue())
                        .counterpartyName(res.counterpartyName())
                        .transactionSummary(res.counterpartyName())
                        .transactionAt(res.transactionDate())
                        .syncedAt(LocalDateTime.now())
                        .build();
                
                newTransactions.add(transactionRepository.save(transaction));
            }
        }

        if (!newTransactions.isEmpty()) {
            analyzeAndSaveAiMetadata(newTransactions);
        }

        return newTransactions.size();
    }

    private void analyzeAndSaveAiMetadata(List<Transaction> transactions) {
        List<String> merchantNames = transactions.stream()
                .map(Transaction::getCounterpartyName)
                .collect(Collectors.toList());

        try {
            AiAnalysisResponse aiResponse = aiServiceClient.analyzeMerchants(merchantNames);
            
            if (aiResponse != null && aiResponse.results() != null) {
                Map<String, AiAnalysisResponse.AiAnalysisResult> resultMap = aiResponse.results().stream()
                        .collect(Collectors.toMap(
                                AiAnalysisResponse.AiAnalysisResult::merchantName,
                                result -> result,
                                (existing, replacement) -> existing
                        ));

                for (Transaction tx : transactions) {
                    AiAnalysisResponse.AiAnalysisResult aiResult = resultMap.get(tx.getCounterpartyName());
                    
                    TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                            .transaction(tx)
                            .aiCategory(aiResult != null ? aiResult.category() : "미분류")
                            .confidenceScore(aiResult != null ? aiResult.confidence() : 0.0)
                            .build();
                    
                    transactionAiMetadataRepository.save(metadata);
                }
            }
        } catch (Exception e) {
            log.error("AI Analysis failed for transactions: {}", e.getMessage());
            for (Transaction tx : transactions) {
                transactionAiMetadataRepository.save(TransactionAiMetadata.builder()
                        .transaction(tx)
                        .aiCategory("미분류")
                        .confidenceScore(0.0)
                        .build());
            }
        }
    }
}
