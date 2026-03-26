package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.TransactionSyncService;
import com.donttaz.donttazapi.ai.domain.CategorySpendingPlan;
import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import com.donttaz.donttazapi.ai.dto.request.AiAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiPlanAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionReanalyzeRequest;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionReanalyzeResponse;
import com.donttaz.donttazapi.ai.dto.response.AiTransactionSyncResponse;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiTransactionService {

    private static final int TEMPORARY_MONTHLY_INCOME_FALLBACK = 3_000_000;
    private static final String UNCATEGORIZED = "미분류";

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAiMetadataRepository transactionAiMetadataRepository;
    private final SpendingPlanRepository spendingPlanRepository;
    private final TransactionSyncService transactionSyncService;
    private final AiServiceClient aiServiceClient;
    private final CurrentUserProvider currentUserProvider;
    private final Environment environment;

    public AiBurndownChartResponse getBurndownChart(String yearMonth) {
        Long userId = currentUserProvider.getCurrentUserId();
        SpendingPlan plan = spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)
                .orElseGet(() -> createLocalDummyBurndownPlan(userId, yearMonth));

        if (plan == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int totalDays = ym.lengthOfMonth();
        int totalBudget = plan.getTotalBudget();

        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(
                userId,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay(),
                Pageable.unpaged()
        ).getContent();

        Map<LocalDate, Integer> dailySpentMap = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getTransactionAt().toLocalDate(),
                        Collectors.summingInt(tx -> tx.getAmount().intValue())
                ));

        List<AiBurndownChartResponse.BurndownChartItem> chartData = new ArrayList<>();
        int accumulatedSpent = 0;
        int currentRemaining = totalBudget;
        LocalDate today = LocalDate.now();
        LocalDate lastDataPoint = ym.equals(YearMonth.now()) ? today : end;

        for (int i = 1; i <= totalDays; i++) {
            LocalDate date = ym.atDay(i);
            int idealRemaining = totalBudget - (int) ((double) totalBudget / totalDays * i);
            accumulatedSpent += dailySpentMap.getOrDefault(date, 0);
            int actualRemaining = totalBudget - accumulatedSpent;

            if (!date.isAfter(lastDataPoint)) {
                currentRemaining = actualRemaining;
            }

            chartData.add(new AiBurndownChartResponse.BurndownChartItem(
                    date,
                    Math.max(0, idealRemaining),
                    date.isAfter(lastDataPoint) ? null : actualRemaining
            ));
        }

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
        SpendingPlan plan = spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged())
                .getContent();

        Map<String, Integer> actualSpendingMap = transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> transactionAiMetadataRepository.findByTransactionId(tx.getId())
                                .map(TransactionAiMetadata::getAiCategory)
                                .orElse(UNCATEGORIZED),
                        Collectors.summingInt(tx -> tx.getAmount().intValue())
                ));

        int totalSpent = 0;
        List<AiSpendingReportResponse.CategoryReportItem> categoryReports = new ArrayList<>();

        for (CategorySpendingPlan categoryPlan : plan.getCategoryPlans()) {
            int spent = actualSpendingMap.getOrDefault(categoryPlan.getCategoryName(), 0);
            totalSpent += spent;

            int rate = categoryPlan.getAllocatedAmount() > 0
                    ? (int) ((double) spent / categoryPlan.getAllocatedAmount() * 100)
                    : 0;

            categoryReports.add(new AiSpendingReportResponse.CategoryReportItem(
                    categoryPlan.getCategoryName(),
                    categoryPlan.getAllocatedAmount(),
                    spent,
                    rate
            ));
        }

        int totalBudget = plan.getTotalBudget();
        int totalRate = totalBudget > 0 ? (int) ((double) totalSpent / totalBudget * 100) : 0;

        return new AiSpendingReportResponse(
                yearMonth,
                totalBudget,
                totalSpent,
                totalRate,
                generateOverallFeedback(totalBudget, totalSpent, totalRate),
                categoryReports
        );
    }

    public SpendingPlanResponse createSpendingPlan(SpendingPlanCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        spendingPlanRepository.deleteByUserIdAndTargetYearMonth(userId, request.targetYearMonth());

        LocalDateTime end = YearMonth.parse(request.targetYearMonth()).atDay(1).atStartOfDay();
        LocalDateTime start = end.minusMonths(3);

        List<Transaction> history = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged())
                .getContent();

        List<AiPlanAnalysisRequest.AiTransactionItem> accountTransactions = new ArrayList<>();
        List<AiPlanAnalysisRequest.AiTransactionItem> cardTransactions = new ArrayList<>();

        for (Transaction transaction : history) {
            String category = transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                    .map(TransactionAiMetadata::getAiCategory)
                    .orElse(UNCATEGORIZED);

            AiPlanAnalysisRequest.AiTransactionItem item = toAiPlanTransactionItem(transaction, category);
            if (isAccountSideTransaction(transaction, category)) {
                accountTransactions.add(item);
            } else {
                cardTransactions.add(item);
            }
        }

        long currentBalance = accountRepository.findByUserId(userId).stream()
                .mapToLong(Account::getBalance)
                .sum();

        User user = userRepository.findById(userId).orElse(null);
        int paydayDay = user != null && user.getSalaryDay() != null ? user.getSalaryDay() : 25;
        int monthlyIncome = request.monthlyIncome() != null && request.monthlyIncome() > 0
                ? request.monthlyIncome()
                : TEMPORARY_MONTHLY_INCOME_FALLBACK;

        if (request.monthlyIncome() == null || request.monthlyIncome() <= 0) {
            log.warn(
                    "[AI Plan] monthlyIncome missing or invalid for userId={}. Using temporary fallback={}",
                    userId,
                    TEMPORARY_MONTHLY_INCOME_FALLBACK
            );
        }

        AiPlanAnalysisResponse aiResponse = aiServiceClient.calculateSpendingPlan(
                new AiPlanAnalysisRequest(
                        request.targetYearMonth(),
                        monthlyIncome,
                        0,
                        currentBalance,
                        paydayDay,
                        accountTransactions,
                        cardTransactions
                )
        );

        AiPlanAnalysisResponse.AiPlanData planData = aiResponse.data();
        int totalBudget = orZero(planData.totalBudget());
        int sealedAmount = orZero(planData.sealedAmount());
        int surplusAmount = planData.surplusAmount() != null
                ? planData.surplusAmount()
                : Math.max(0, monthlyIncome - totalBudget - sealedAmount);

        SpendingPlan spendingPlan = SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(request.targetYearMonth())
                .monthlyIncome(monthlyIncome)
                .totalBudget(totalBudget)
                .surplusAmount(surplusAmount)
                .sealedAmount(sealedAmount)
                .build();

        List<AiPlanAnalysisResponse.RecommendedCategoryPlan> recommendedCategories =
                planData.categories() != null ? planData.categories() : List.of();

        for (AiPlanAnalysisResponse.RecommendedCategoryPlan recommendedCategory : recommendedCategories) {
            if (recommendedCategory.recommendedAmount() == null) {
                continue;
            }

            spendingPlan.addCategoryPlan(CategorySpendingPlan.builder()
                    .categoryName(recommendedCategory.categoryName())
                    .allocatedAmount(recommendedCategory.recommendedAmount())
                    .aiComment(recommendedCategory.comment())
                    .build());
        }

        SpendingPlan savedPlan = spendingPlanRepository.save(spendingPlan);

        return new SpendingPlanResponse(
                savedPlan.getId(),
                savedPlan.getTargetYearMonth(),
                planData.referenceStartMonth(),
                planData.referenceEndMonth(),
                savedPlan.getTotalBudget(),
                savedPlan.getSealedAmount(),
                savedPlan.getSurplusAmount(),
                planData.paydayDay() != null ? planData.paydayDay() : paydayDay,
                planData.riskScore(),
                planData.budgetStrategy(),
                savedPlan.getCategoryPlans().stream()
                        .map(plan -> new SpendingPlanResponse.CategoryPlanItem(
                                plan.getCategoryName(),
                                plan.getAllocatedAmount(),
                                plan.getAiComment()
                        ))
                        .collect(Collectors.toList()),
                planData.personalizedRecommendations() == null
                        ? List.of()
                        : planData.personalizedRecommendations().stream()
                                .map(recommendation -> new SpendingPlanResponse.RecommendationItem(
                                        recommendation.type(),
                                        recommendation.categoryName(),
                                        recommendation.message()
                                ))
                                .collect(Collectors.toList()),
                planData.spendingPersona()
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
                                .aiCategory(UNCATEGORIZED)
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

        Page<Transaction> transactionPage = transactionRepository.searchWithAiMetadata(userId, startDate, endDate, pageable);

        List<AiCategorizedListResponse.AiCategorizedTransactionItem> items = transactionPage.getContent().stream()
                .map(transaction -> {
                    TransactionAiMetadata metadata = transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                            .orElse(null);

                    return new AiCategorizedListResponse.AiCategorizedTransactionItem(
                            transaction.getId(),
                            transaction.getTransactionAt(),
                            transaction.getCounterpartyName(),
                            transaction.getAmount(),
                            metadata != null ? metadata.getAiCategory() : UNCATEGORIZED,
                            metadata != null ? metadata.getConfidenceScore() : 0.0,
                            metadata != null ? metadata.getIsModified() : false
                    );
                })
                .collect(Collectors.toList());

        return new AiCategorizedListResponse(transactionPage.getTotalElements(), items);
    }

    public AiTransactionSyncResponse syncTransactions(AiTransactionSyncRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        User authUser = userRepository.findById(userId).orElse(null);
        log.info(
                "[AI Auth Debug] Authenticated User - ID: {}, Email: {}",
                userId,
                authUser != null ? authUser.getEmail() : "Unknown"
        );

        List<Account> accounts = accountRepository.findByUserId(userId);
        int totalSyncedCount = 0;
        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now().minusMonths(3);
        LocalDate endDate = request.endDate() != null ? request.endDate() : LocalDate.now();

        for (Account account : accounts) {
            List<Transaction> newTransactions = transactionSyncService.syncTransactions(account, startDate, endDate);
            if (!newTransactions.isEmpty()) {
                analyzeAndSaveAiMetadata(newTransactions);
            }
            totalSyncedCount += newTransactions.size();
        }

        List<Transaction> unanalyzedTransactions = transactionRepository.findByUserId(userId).stream()
                .filter(this::needsAiAnalysis)
                .collect(Collectors.toList());

        if (!unanalyzedTransactions.isEmpty()) {
            log.info("[AI Sync] Found {} transactions requiring AI analysis.", unanalyzedTransactions.size());
            analyzeAndSaveAiMetadata(unanalyzedTransactions);
        }

        return new AiTransactionSyncResponse(totalSyncedCount + unanalyzedTransactions.size(), LocalDateTime.now());
    }

    public AiTransactionReanalyzeResponse reanalyzeTransactions(AiTransactionReanalyzeRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDate endDate = request.endDate() != null ? request.endDate() : LocalDate.now();
        LocalDate startDate = request.startDate() != null ? request.startDate() : endDate.minusMonths(3);
        boolean force = Boolean.TRUE.equals(request.force());

        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(
                userId,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay(),
                Pageable.unpaged()
        ).getContent();

        List<Transaction> targetTransactions = transactions.stream()
                .filter(transaction -> force || needsAiAnalysis(transaction))
                .collect(Collectors.toList());

        if (!targetTransactions.isEmpty()) {
            reanalyzeAndSaveAiMetadata(targetTransactions);
        }

        return new AiTransactionReanalyzeResponse(
                startDate,
                endDate,
                transactions.size(),
                targetTransactions.size(),
                force,
                LocalDateTime.now()
        );
    }

    private SpendingPlan createLocalDummyBurndownPlan(Long userId, String yearMonth) {
        if (!environment.acceptsProfiles(Profiles.of("local", "dev"))) {
            return null;
        }

        log.info("No spending plan found for userId={} yearMonth={}. Returning local dummy burndown data.", userId, yearMonth);

        return SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(yearMonth)
                .monthlyIncome(3_000_000)
                .totalBudget(2_500_000)
                .surplusAmount(500_000)
                .sealedAmount(0)
                .build();
    }

    private String generateOverallFeedback(int budget, int spent, int rate) {
        if (rate <= 100) {
            return String.format("이번 달은 예산 내에서 아주 잘 소비하셨어요! %d원 정도 여유가 있네요.", budget - spent);
        }
        return String.format("전체 예산을 %d원 초과했어요. 다음 달에는 조금 더 계획적인 소비가 필요해 보여요.", spent - budget);
    }

    private AiPlanAnalysisRequest.AiTransactionItem toAiPlanTransactionItem(Transaction transaction, String category) {
        String transactionDate = transaction.getTransactionAt() != null
                ? transaction.getTransactionAt().toLocalDate().toString()
                : LocalDate.now().toString();
        String txType = transaction.getTxType() != null ? transaction.getTxType() : "WITHDRAWAL";

        return new AiPlanAnalysisRequest.AiTransactionItem(
                transaction.getId() != null ? transaction.getId().toString() : null,
                transactionDate,
                transaction.getAmount(),
                transaction.getCounterpartyName(),
                category,
                null,
                txType
        );
    }

    private boolean isAccountSideTransaction(Transaction transaction, String category) {
        if ("DEPOSIT".equalsIgnoreCase(transaction.getTxType())) {
            return true;
        }
        if (category == null || category.isBlank() || UNCATEGORIZED.equals(category)) {
            return true;
        }
        return "금융/보험".equals(category);
    }

    private void analyzeAndSaveAiMetadata(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return;
        }

        List<AiAnalysisRequest.TransactionItem> items = toAnalysisItems(transactions);
        log.info("[AI Analysis] Requesting categorization for {} transactions", items.size());

        try {
            AiAnalysisResponse aiResponse = aiServiceClient.analyzeTransactions(new AiAnalysisRequest(items));
            Map<Long, AiAnalysisResponse.AiAnalysisResult> resultMap = mapResultsByTransactionId(aiResponse);

            for (Transaction transaction : transactions) {
                AiAnalysisResponse.AiAnalysisResult result = resultMap.get(transaction.getId());
                upsertAiMetadata(
                        transaction,
                        result != null ? result.category() : UNCATEGORIZED,
                        result != null ? result.confidence() : 0.0
                );
            }
        } catch (Exception exception) {
            log.error("[AI Analysis] AI Analysis failed for transactions: {}", exception.getMessage(), exception);
            for (Transaction transaction : transactions) {
                upsertAiMetadata(transaction, UNCATEGORIZED, 0.0);
            }
        }
    }

    private void reanalyzeAndSaveAiMetadata(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return;
        }

        List<AiAnalysisRequest.TransactionItem> items = toAnalysisItems(transactions);
        log.info("[AI Reanalyze] Requesting categorization for {} transactions", items.size());

        try {
            AiAnalysisResponse aiResponse = aiServiceClient.analyzeTransactions(new AiAnalysisRequest(items));
            Map<Long, AiAnalysisResponse.AiAnalysisResult> resultMap = mapResultsByTransactionId(aiResponse);

            for (Transaction transaction : transactions) {
                AiAnalysisResponse.AiAnalysisResult result = resultMap.get(transaction.getId());
                upsertAiMetadata(
                        transaction,
                        result != null ? result.category() : UNCATEGORIZED,
                        result != null ? result.confidence() : 0.0
                );
            }
        } catch (Exception exception) {
            log.error("[AI Reanalyze] AI Analysis failed for transactions: {}", exception.getMessage(), exception);
            for (Transaction transaction : transactions) {
                upsertAiMetadata(transaction, UNCATEGORIZED, 0.0);
            }
        }
    }

    private List<AiAnalysisRequest.TransactionItem> toAnalysisItems(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> new AiAnalysisRequest.TransactionItem(
                        transaction.getId() != null ? transaction.getId().toString() : null,
                        transaction.getTransactionAt() != null
                                ? transaction.getTransactionAt().toLocalDate().toString()
                                : LocalDate.now().toString(),
                        transaction.getAmount(),
                        transaction.getCounterpartyName(),
                        transaction.getTxType()
                ))
                .collect(Collectors.toList());
    }

    private Map<Long, AiAnalysisResponse.AiAnalysisResult> mapResultsByTransactionId(AiAnalysisResponse aiResponse) {
        if (aiResponse == null || aiResponse.data() == null) {
            return Map.of();
        }

        return aiResponse.data().stream()
                .filter(result -> result.transactionId() != null && !result.transactionId().isBlank())
                .collect(Collectors.toMap(
                        result -> Long.parseLong(result.transactionId()),
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }

    private boolean needsAiAnalysis(Transaction transaction) {
        return transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                .map(metadata -> isUncategorized(metadata.getAiCategory()))
                .orElse(true);
    }

    private boolean isUncategorized(String category) {
        return category == null || category.isBlank() || UNCATEGORIZED.equals(category);
    }

    private void upsertAiMetadata(Transaction transaction, String category, Double confidenceScore) {
        TransactionAiMetadata metadata = transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                .orElseGet(() -> TransactionAiMetadata.builder()
                        .transaction(transaction)
                        .aiCategory(category)
                        .confidenceScore(confidenceScore)
                        .build());

        metadata.applyAiAnalysis(category, confidenceScore);
        transactionAiMetadataRepository.save(metadata);
    }

    private int orZero(Integer value) {
        return value != null ? value : 0;
    }
}
