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

import com.donttaz.donttazapi.ai.domain.CategorySpendingPlan;
import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import com.donttaz.donttazapi.ai.dto.request.AiPlanAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;
import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;

import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;

import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiTransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAiMetadataRepository transactionAiMetadataRepository;
    private final SpendingPlanRepository spendingPlanRepository;
    private final BankingAccountQueryService bankingAccountQueryService;
    private final AiServiceClient aiServiceClient;
    private final CurrentUserProvider currentUserProvider;

    public AiBurndownChartResponse getBurndownChart(String yearMonth) {
        Long userId = currentUserProvider.getCurrentUserId();
        SpendingPlan plan = spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int totalDays = ym.lengthOfMonth();
        int totalBudget = plan.getTotalBudget();

        // 1. 일자별 실제 지출액 집계
        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(
                userId, start.atStartOfDay(), end.plusDays(1).atStartOfDay(), Pageable.unpaged()).getContent();

        Map<LocalDate, Integer> dailySpentMap = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getTransactionAt().toLocalDate(),
                        Collectors.summingInt(tx -> tx.getAmount().intValue())
                ));

        // 2. 일자별 차트 데이터 생성
        List<AiBurndownChartResponse.BurndownChartItem> chartData = new ArrayList<>();
        int accumulatedSpent = 0;
        int currentRemaining = totalBudget;
        LocalDate today = LocalDate.now();
        
        // 조회 대상 연월이 현재 달인 경우 오늘까지만 실제 데이터를 보여주고, 지난 달인 경우 말일까지 보여줌
        LocalDate lastDataPoint = ym.equals(YearMonth.now()) ? today : end;

        for (int i = 1; i <= totalDays; i++) {
            LocalDate date = ym.atDay(i);
            
            // 이상적 잔액 계산 (매일 일정하게 감소하여 말일 자정에 0이 되는 선)
            int idealRemaining = totalBudget - (int) ((double) totalBudget / totalDays * i);
            
            // 실제 잔액 계산 (자정 기준이므로 해당 일자의 지출까지 반영)
            accumulatedSpent += dailySpentMap.getOrDefault(date, 0);
            int actualRemaining = totalBudget - accumulatedSpent;

            // 데이터 포인트 추가 (미래 날짜는 실제 잔액을 0 또는 이전 값으로 채우지 않고 흐름만 유지)
            if (!date.isAfter(lastDataPoint)) {
                currentRemaining = actualRemaining;
            }

            chartData.add(new AiBurndownChartResponse.BurndownChartItem(
                    date,
                    Math.max(0, idealRemaining),
                    date.isAfter(lastDataPoint) ? null : actualRemaining // 미래 데이터는 null 처리 (프론트 렌더링용)
            ));
        }

        // 3. 생존 여부 판단 (오늘 기준 실제 잔액이 이상적 잔액보다 크거나 같으면 생존)
        int todayIdeal = totalBudget - (int) ((double) totalBudget / totalDays * lastDataPoint.getDayOfMonth());
        boolean isSurviving = currentRemaining >= todayIdeal;

        return new AiBurndownChartResponse(
                yearMonth,
                totalBudget,
                currentRemaining,
                isSurviving,
                chartData
        );
    }

    public AiSpendingReportResponse getSpendingReport(String yearMonth) {
        Long userId = currentUserProvider.getCurrentUserId();
        
        // 1. 해당 월의 플랜 조회 (없으면 예외)
        SpendingPlan plan = spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST)); // 플랜이 먼저 생성되어야 함

        // 2. 해당 월의 실제 거래 내역 조회
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();
        
        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged()).getContent();

        // 3. 카테고리별 실제 지출 합계 계산
        Map<String, Integer> actualSpendingMap = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> {
                            TransactionAiMetadata m = transactionAiMetadataRepository.findByTransactionId(tx.getId()).orElse(null);
                            return m != null ? m.getAiCategory() : "미분류";
                        },
                        Collectors.summingInt(tx -> tx.getAmount().intValue())
                ));

        // 4. 카테고리별 리포트 항목 생성
        int totalSpent = 0;
        List<AiSpendingReportResponse.CategoryReportItem> categoryReports = new ArrayList<>();

        for (CategorySpendingPlan cp : plan.getCategoryPlans()) {
            int spent = actualSpendingMap.getOrDefault(cp.getCategoryName(), 0);
            totalSpent += spent;
            
            int rate = cp.getAllocatedAmount() > 0 ? (int) ((double) spent / cp.getAllocatedAmount() * 100) : 0;
            
            categoryReports.add(new AiSpendingReportResponse.CategoryReportItem(
                    cp.getCategoryName(),
                    cp.getAllocatedAmount(),
                    spent,
                    rate
            ));
        }

        // 5. 전체 통계 및 피드백 생성
        int totalBudget = plan.getTotalBudget();
        int totalRate = totalBudget > 0 ? (int) ((double) totalSpent / totalBudget * 100) : 0;
        
        String feedback = generateOverallFeedback(totalBudget, totalSpent, totalRate);

        return new AiSpendingReportResponse(
                yearMonth,
                totalBudget,
                totalSpent,
                totalRate,
                feedback,
                categoryReports
        );
    }

    private String generateOverallFeedback(int budget, int spent, int rate) {
        if (rate <= 100) {
            return String.format("이번 달은 예산 내에서 아주 잘 소비하셨어요! %d원 정도 여유가 있네요.", budget - spent);
        } else {
            return String.format("전체 예산을 %d원 초과했어요. 다음 달에는 조금 더 계획적인 소비가 필요해 보여요.", spent - budget);
        }
    }

    public SpendingPlanResponse createSpendingPlan(SpendingPlanCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        
        // 1. 기존 플랜이 있으면 삭제 (덮어쓰기 전략)
        spendingPlanRepository.deleteByUserIdAndTargetYearMonth(userId, request.targetYearMonth());

        // 2. 최근 3개월 소비 데이터 집계
        LocalDateTime end = YearMonth.parse(request.targetYearMonth()).atDay(1).atStartOfDay();
        LocalDateTime start = end.minusMonths(3);
        
        // Transaction + Metadata 조인 결과를 가져와서 카테고리별 합계 계산
        // (간단하게 구현하기 위해 전체를 가져와 메모리에서 집계 - 실무에선 쿼리로 처리 권장)
        List<Transaction> history = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged()).getContent();
        
        Map<String, Long> categoryStats = history.stream()
                .collect(Collectors.groupingBy(
                        tx -> {
                            TransactionAiMetadata m = transactionAiMetadataRepository.findByTransactionId(tx.getId()).orElse(null);
                            return m != null ? m.getAiCategory() : "미분류";
                        },
                        Collectors.summingLong(Transaction::getAmount)
                ));

        List<AiPlanAnalysisRequest.CategorySummary> aiHistory = categoryStats.entrySet().stream()
                .map(e -> new AiPlanAnalysisRequest.CategorySummary(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 3. AI 서버 호출
        AiPlanAnalysisResponse aiResponse = aiServiceClient.calculateSpendingPlan(
                new AiPlanAnalysisRequest(request.monthlyIncome(), aiHistory));

        // 4. 결과 저장
        int totalBudget = aiResponse.totalBudget();
        int surplusAmount = Math.max(0, request.monthlyIncome() - totalBudget);

        SpendingPlan spendingPlan = SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(request.targetYearMonth())
                .monthlyIncome(request.monthlyIncome())
                .totalBudget(totalBudget)
                .surplusAmount(surplusAmount)
                .build();

        for (AiPlanAnalysisResponse.RecommendedCategoryPlan res : aiResponse.categories()) {
            spendingPlan.addCategoryPlan(CategorySpendingPlan.builder()
                    .categoryName(res.categoryName())
                    .allocatedAmount(res.recommendedAmount())
                    .aiComment(res.comment())
                    .build());
        }

        SpendingPlan savedPlan = spendingPlanRepository.save(spendingPlan);

        // 5. 금고 자동 이체 (주석 처리)
        /* 
         * TODO: 향후 산출된 surplusAmount를 사용자의 Vault(금고)로 자동 이체하는 로직 구현 필요
         * - 사용자의 대표 계좌 확인
         * - VaultService.deposit() 또는 관련 이체 API 호출
         * log.info("Surplus amount {} calculated for user {}. Automatic vault transfer pending.", surplusAmount, userId);
         */

        return new SpendingPlanResponse(
                savedPlan.getId(),
                savedPlan.getTargetYearMonth(),
                savedPlan.getTotalBudget(),
                savedPlan.getSurplusAmount(),
                savedPlan.getCategoryPlans().stream()
                        .map(p -> new SpendingPlanResponse.CategoryPlanItem(
                                p.getCategoryName(), p.getAllocatedAmount(), p.getAiComment()))
                        .collect(Collectors.toList())
        );
    }

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
