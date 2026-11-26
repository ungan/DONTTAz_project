package com.donttaz.donttazapi.ai.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.response.DailyTransactionAmount;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.TransactionSyncService;
import com.donttaz.donttazapi.ai.domain.CategorySpendingPlan;
import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import com.donttaz.donttazapi.ai.dto.request.AiAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiPlanAnalysisRequest;
import com.donttaz.donttazapi.ai.dto.request.AiSpendingReportGenerateRequest;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionReanalyzeRequest;
import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportGenerateResponse;
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
import com.donttaz.donttazapi.payment.domain.Payment;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    // 거래 유형은 저장 경로마다 다르다: 동기화(TransactionSyncService)는 'DEPOSIT'/'WITHDRAWAL'로 정규화하고,
    // 이체(AccountTransferService)는 은행 원본 코드 '1'(입금)/'2'(출금)로 저장한다.
    // 입금 판정은 이 기준 하나로 한다. TransactionRepository.sumSpendingByDay 의 조건과 같은 기준이다.
    private static final Set<String> DEPOSIT_TX_TYPES = Set.of("DEPOSIT", "1");
    private static final Set<String> WITHDRAWAL_TX_TYPES = Set.of("WITHDRAWAL", "2");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionAiMetadataRepository transactionAiMetadataRepository;
    private final SpendingPlanRepository spendingPlanRepository;
    private final TransactionSyncService transactionSyncService;
    private final AiServiceClient aiServiceClient;
    private final CurrentUserProvider currentUserProvider;
    private final Environment environment;
    private final PaymentRepository paymentRepository;

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

        // 개선(2026-11-26): 일별 합계만 필요하므로 DB에서 날짜별로 집계한다.
        //   전량 적재(unpaged)는 한 달 거래를 전부 엔티티로 올렸고, 500건 페이징은 적재량은 묶었지만
        //   페이지마다 OFFSET 조회와 count 쿼리가 붙어 오히려 느렸다. 집계는 쿼리 1번에 최대 31행.
        // 수정(2026-11-26): 예전 코드는 입금까지 지출로 더해 월급날 남은 예산이 급감했다. 이제 입금은 뺀다.
        Map<LocalDate, Integer> dailySpentMap = transactionRepository.sumSpendingByDay(
                        userId,
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()
                )
                .stream()
                .collect(Collectors.toMap(
                        DailyTransactionAmount::day,
                        row -> row.totalAmount().intValue()
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
        Optional<SpendingPlan> plan = spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth);
        User user = Optional.ofNullable(userRepository.findById(userId))
                .orElse(Optional.empty())
                .orElse(null);
        int paydayDay = user != null && user.getSalaryDay() != null ? user.getSalaryDay() : 25;

        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Transaction> transactions = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged())
                .getContent();
        Map<Long, TransactionAiMetadata> metadataByTransactionId = mapAiMetadataByTransactionId(transactions);
        Map<String, Payment> paymentByTransactionId = mapPaymentsByTransactionId(userId, transactions);

        List<AiSpendingReportGenerateRequest.AiTransactionItem> accountTransactions = new ArrayList<>();
        List<AiSpendingReportGenerateRequest.AiTransactionItem> cardTransactions = new ArrayList<>();

        for (Transaction transaction : transactions) {
            TransactionAiMetadata metadata = metadataByTransactionId.get(transaction.getId());
            Payment payment = paymentByTransactionId.get(transaction.getSourceTxId());
            String category = resolvePlanCategory(transaction, metadata);
            String counterpartyName = resolvePlanCounterpartyName(transaction, payment);
            String memo = resolvePlanMemo(transaction, payment);

            AiSpendingReportGenerateRequest.AiTransactionItem item =
                    toAiReportTransactionItem(transaction, counterpartyName, category, memo);
            if (isAccountSideTransaction(transaction, payment, category)) {
                accountTransactions.add(item);
            } else {
                cardTransactions.add(item);
            }
        }

        AiSpendingReportGenerateRequest request = new AiSpendingReportGenerateRequest(
                yearMonth,
                paydayDay,
                plan.map(this::toReportPlanSummary).orElse(null),
                cardTransactions,
                accountTransactions
        );

        try {
            AiSpendingReportGenerateResponse reportResponse = aiServiceClient.calculateSpendingReport(request);
            if (reportResponse == null || reportResponse.data() == null) {
                log.warn("AI spending report response is empty. Falling back to local aggregation for yearMonth={}", yearMonth);
                return buildFallbackSpendingReport(yearMonth, plan.orElse(null), transactions);
            }

            if (hasSparseCategoryReports(reportResponse, transactions)) {
                log.warn(
                        "AI spending report returned sparse categories for yearMonth={}. Falling back to local aggregation.",
                        yearMonth
                );
                return buildFallbackSpendingReport(yearMonth, plan.orElse(null), transactions);
            }

            return toSpendingReportResponse(reportResponse);
        } catch (RuntimeException exception) {
            log.warn(
                    "AI spending report generation failed for yearMonth={}. Falling back to local/example data.",
                    yearMonth,
                    exception
            );
            return buildFallbackSpendingReport(yearMonth, plan.orElse(null), transactions);
        }
    }

    public SpendingPlanResponse createSpendingPlan(SpendingPlanCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        spendingPlanRepository.deleteByUserIdAndTargetYearMonth(userId, request.targetYearMonth());

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

        YearMonth targetYearMonth = YearMonth.parse(request.targetYearMonth());
        LocalDate cycleEndDate = resolvePaydayDate(targetYearMonth, paydayDay);
        LocalDate cycleStartDate = cycleEndDate.minusMonths(3);
        LocalDateTime start = cycleStartDate.atStartOfDay();
        LocalDateTime end = cycleEndDate.plusDays(1).atStartOfDay();

        List<Transaction> history = transactionRepository.searchWithAiMetadata(userId, start, end, Pageable.unpaged())
                .getContent();
        Map<Long, TransactionAiMetadata> metadataByTransactionId = mapAiMetadataByTransactionId(history);
        Map<String, Payment> paymentByTransactionId = mapPaymentsByTransactionId(userId, history);

        List<AiPlanAnalysisRequest.AiTransactionItem> accountTransactions = new ArrayList<>();
        List<AiPlanAnalysisRequest.AiTransactionItem> cardTransactions = new ArrayList<>();

        for (Transaction transaction : history) {
            TransactionAiMetadata metadata = metadataByTransactionId.get(transaction.getId());
            Payment payment = paymentByTransactionId.get(transaction.getSourceTxId());
            String category = resolvePlanCategory(transaction, metadata);
            String counterpartyName = resolvePlanCounterpartyName(transaction, payment);
            String memo = resolvePlanMemo(transaction, payment);

            AiPlanAnalysisRequest.AiTransactionItem item =
                    toAiPlanTransactionItem(transaction, counterpartyName, category, memo);
            if (isAccountSideTransaction(transaction, payment, category)) {
                accountTransactions.add(item);
            } else {
                cardTransactions.add(item);
            }
        }

        long currentBalance = accountRepository.findByUserId(userId).stream()
                .mapToLong(Account::getBalance)
                .sum();

        log.info(
                "[AI Plan] Prepared request userId={} yearMonth={} monthlyIncome={} currentBalance={} paydayDay={} periodStart={} periodEnd={} historyCount={} accountTxCount={} cardTxCount={} accountSample={} cardSample={}",
                userId,
                request.targetYearMonth(),
                monthlyIncome,
                currentBalance,
                paydayDay,
                cycleStartDate,
                cycleEndDate,
                history.size(),
                accountTransactions.size(),
                cardTransactions.size(),
                summarizePlanTransactions(accountTransactions),
                summarizePlanTransactions(cardTransactions)
        );

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

        if (totalBudget <= 0 && recommendedCategories.isEmpty()) {
            log.warn(
                    "[AI Plan] AI returned sparse plan userId={} yearMonth={} totalBudget={} sealedAmount={} surplusAmount={} categoryPlanCount={} strategy={} persona={}",
                    userId,
                    request.targetYearMonth(),
                    totalBudget,
                    sealedAmount,
                    surplusAmount,
                    recommendedCategories.size(),
                    planData.budgetStrategy(),
                    planData.spendingPersona()
            );
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효한 소비 플랜을 생성하지 못했습니다.");
        }

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

    private AiSpendingReportGenerateRequest.ReportPlanSummary toReportPlanSummary(SpendingPlan plan) {
        List<AiSpendingReportGenerateRequest.CategoryPlanItem> categoryPlans = plan.getCategoryPlans().stream()
                .map(categoryPlan -> new AiSpendingReportGenerateRequest.CategoryPlanItem(
                        categoryPlan.getCategoryName(),
                        categoryPlan.getAllocatedAmount(),
                        categoryPlan.getAiComment()
                ))
                .collect(Collectors.toList());

        return new AiSpendingReportGenerateRequest.ReportPlanSummary(
                plan.getTotalBudget(),
                orZero(plan.getSealedAmount()),
                categoryPlans
        );
    }

    private AiSpendingReportResponse toSpendingReportResponse(AiSpendingReportGenerateResponse reportResponse) {
        AiSpendingReportGenerateResponse.ReportData data = reportResponse.data();

        List<AiSpendingReportResponse.CategoryReportItem> categoryReports =
                (data.categoryReports() == null ? List.<AiSpendingReportGenerateResponse.CategoryReportItem>of() : data.categoryReports())
                        .stream()
                        .map(item -> new AiSpendingReportResponse.CategoryReportItem(
                                item.categoryName(),
                                orZero(item.allocatedAmount()),
                                orZero(item.spentAmount()),
                                orZero(item.categoryRate()),
                                item.budgetAchievementRate() != null
                                        ? item.budgetAchievementRate()
                                        : calculateBudgetAchievementRate(item.allocatedAmount(), item.spentAmount()),
                                item.feedback()
                        ))
                        .sorted(Comparator
                                .comparingInt(AiSpendingReportResponse.CategoryReportItem::spentAmount)
                                .reversed()
                                .thenComparing(AiSpendingReportResponse.CategoryReportItem::categoryName,
                                        Comparator.nullsLast(String::compareTo)))
                        .collect(Collectors.toList());

        List<AiSpendingReportResponse.PersonalizedFeedbackItem> personalizedFeedback =
                (data.personalizedFeedback() == null ? List.<AiSpendingReportGenerateResponse.PersonalizedFeedbackItem>of()
                        : data.personalizedFeedback())
                        .stream()
                        .map(item -> new AiSpendingReportResponse.PersonalizedFeedbackItem(
                                item.type(),
                                item.categoryName(),
                                item.message()
                        ))
                        .collect(Collectors.toList());

        return new AiSpendingReportResponse(
                data.yearMonth(),
                orZero(data.sealedAmount()),
                orZero(data.totalBudget()),
                orZero(data.totalSpent()),
                orZero(data.achievementRate()),
                data.spendingPersona(),
                data.aiOverallFeedback(),
                categoryReports,
                personalizedFeedback
        );
    }

    private AiSpendingReportResponse buildFallbackSpendingReport(
            String yearMonth,
            SpendingPlan plan,
            List<Transaction> transactions
    ) {
        Map<String, Integer> actualSpendingMap = transactions.stream()
                .filter(transaction -> !isDeposit(transaction))
                .collect(Collectors.groupingBy(
                        transaction -> transactionAiMetadataRepository.findByTransactionId(transaction.getId())
                                .map(TransactionAiMetadata::getAiCategory)
                                .orElse(UNCATEGORIZED),
                        Collectors.summingInt(transaction -> transaction.getAmount().intValue())
                ));

        int totalSpent = actualSpendingMap.values().stream().mapToInt(Integer::intValue).sum();
        int totalBudget = plan != null ? orZero(plan.getTotalBudget()) : 0;
        int sealedAmount = plan != null ? orZero(plan.getSealedAmount()) : 0;

        Map<String, Integer> allocatedMap = plan == null
                ? Map.of()
                : plan.getCategoryPlans().stream().collect(Collectors.toMap(
                        CategorySpendingPlan::getCategoryName,
                        categoryPlan -> orZero(categoryPlan.getAllocatedAmount()),
                        (left, right) -> left
                ));

        List<String> categories = new ArrayList<>();
        allocatedMap.keySet().forEach(categories::add);
        actualSpendingMap.keySet().stream()
                .filter(category -> !categories.contains(category))
                .forEach(categories::add);

        List<AiSpendingReportResponse.CategoryReportItem> categoryReports = categories.stream()
                .map(category -> {
                    int allocatedAmount = allocatedMap.getOrDefault(category, 0);
                    int spentAmount = actualSpendingMap.getOrDefault(category, 0);
                    double categoryRate = totalSpent > 0 ? Math.round(((double) spentAmount / totalSpent) * 1000) / 10.0 : 0.0;
                    String feedback = allocatedAmount == 0
                            ? "계획에 없던 지출 항목입니다."
                            : spentAmount > allocatedAmount ? "계획 대비 초과 소비입니다." : "계획 범위 내 소비입니다.";

                    return new AiSpendingReportResponse.CategoryReportItem(
                            category,
                            allocatedAmount,
                            spentAmount,
                            categoryRate,
                            calculateBudgetAchievementRate(allocatedAmount, spentAmount),
                            feedback
                    );
                })
                .sorted(Comparator
                        .comparingInt(AiSpendingReportResponse.CategoryReportItem::spentAmount)
                        .reversed()
                        .thenComparing(AiSpendingReportResponse.CategoryReportItem::categoryName,
                                Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());

        double achievementRate = totalBudget > 0 ? Math.round(((double) totalSpent / totalBudget) * 1000) / 10.0 : 0.0;
        if (categoryReports.isEmpty()) {
            return buildExampleSpendingReport(yearMonth, plan);
        }

        return new AiSpendingReportResponse(
                yearMonth,
                sealedAmount,
                totalBudget,
                totalSpent,
                achievementRate,
                null,
                generateOverallFeedback(totalBudget, totalSpent, (int) achievementRate),
                categoryReports,
                buildFallbackFeedback(totalBudget, totalSpent, achievementRate)
        );
    }

    private boolean hasSparseCategoryReports(
            AiSpendingReportGenerateResponse reportResponse,
            List<Transaction> transactions
    ) {
        if (transactions.isEmpty()) {
            return false;
        }

        AiSpendingReportGenerateResponse.ReportData data = reportResponse.data();
        return data == null || data.categoryReports() == null || data.categoryReports().isEmpty();
    }

    private List<AiSpendingReportResponse.PersonalizedFeedbackItem> buildFallbackFeedback(
            int totalBudget,
            int totalSpent,
            double achievementRate
    ) {
        List<AiSpendingReportResponse.PersonalizedFeedbackItem> feedback = new ArrayList<>();
        feedback.add(new AiSpendingReportResponse.PersonalizedFeedbackItem(
                "info",
                null,
                "AI report is unavailable, so this summary is generated from saved transactions."
        ));

        if (totalBudget > 0) {
            feedback.add(new AiSpendingReportResponse.PersonalizedFeedbackItem(
                    achievementRate > 100.0 ? "warning" : "positive",
                    null,
                    achievementRate > 100.0
                            ? String.format("You spent %.1f%% of your budget this month.", achievementRate)
                            : String.format("You stayed within budget and used %.1f%% of it.", achievementRate)
            ));
        } else if (totalSpent > 0) {
            feedback.add(new AiSpendingReportResponse.PersonalizedFeedbackItem(
                    "info",
                    null,
                    "There is spending data, but no saved plan was found for this month."
            ));
        }

        return feedback;
    }

    private AiSpendingReportResponse buildExampleSpendingReport(String yearMonth, SpendingPlan plan) {
        int totalBudget = plan != null ? Math.max(orZero(plan.getTotalBudget()), 900_000) : 900_000;
        int sealedAmount = plan != null ? orZero(plan.getSealedAmount()) : 150_000;

        List<AiSpendingReportResponse.CategoryReportItem> categoryReports;
        if (plan != null && !plan.getCategoryPlans().isEmpty()) {
            List<CategorySpendingPlan> categoryPlans = plan.getCategoryPlans().stream()
                    .sorted(Comparator.comparing(CategorySpendingPlan::getCategoryName, Comparator.nullsLast(String::compareTo)))
                    .toList();
            int allocatedTotal = categoryPlans.stream()
                    .map(CategorySpendingPlan::getAllocatedAmount)
                    .filter(amount -> amount != null && amount > 0)
                    .mapToInt(Integer::intValue)
                    .sum();

            categoryReports = categoryPlans.stream()
                    .map(categoryPlan -> {
                        int allocatedAmount = Math.max(orZero(categoryPlan.getAllocatedAmount()), 100_000);
                        int spentAmount = Math.max((int) Math.round(allocatedAmount * 0.82), 30_000);
                        double categoryRate = allocatedTotal > 0
                                ? Math.round(((double) spentAmount / Math.max(allocatedTotal, 1)) * 1000) / 10.0
                                : 0.0;
                        return new AiSpendingReportResponse.CategoryReportItem(
                                categoryPlan.getCategoryName(),
                                allocatedAmount,
                                spentAmount,
                                categoryRate,
                                calculateBudgetAchievementRate(allocatedAmount, spentAmount),
                                "Fallback example data is shown while the AI report is unavailable."
                        );
                    })
                    .sorted(Comparator
                            .comparingInt(AiSpendingReportResponse.CategoryReportItem::spentAmount)
                            .reversed()
                            .thenComparing(AiSpendingReportResponse.CategoryReportItem::categoryName,
                                    Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());
        } else {
            categoryReports = List.of(
                    new AiSpendingReportResponse.CategoryReportItem("Food", 320000, 280000, 35.0, 87.5,
                            "Fallback example data is shown while the AI report is unavailable."),
                    new AiSpendingReportResponse.CategoryReportItem("Shopping", 220000, 180000, 22.5, 81.8,
                            "Fallback example data is shown while the AI report is unavailable."),
                    new AiSpendingReportResponse.CategoryReportItem("Living", 360000, 340000, 42.5, 94.4,
                            "Fallback example data is shown while the AI report is unavailable.")
            );
        }

        int totalSpent = categoryReports.stream()
                .map(AiSpendingReportResponse.CategoryReportItem::spentAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();
        double achievementRate = totalBudget > 0 ? Math.round(((double) totalSpent / totalBudget) * 1000) / 10.0 : 0.0;

        return new AiSpendingReportResponse(
                yearMonth,
                sealedAmount,
                totalBudget,
                totalSpent,
                achievementRate,
                "Fallback",
                "AI report generation failed, so example report data is being shown.",
                categoryReports,
                List.of(
                        new AiSpendingReportResponse.PersonalizedFeedbackItem(
                                "info",
                                null,
                                "This is example data to keep the report screen usable."
                        ),
                        new AiSpendingReportResponse.PersonalizedFeedbackItem(
                                "action",
                                categoryReports.get(0).categoryName(),
                                "Reconnect AI report generation and replace this example with real analysis."
                        )
                )
        );
    }

    private double calculateBudgetAchievementRate(Integer allocatedAmount, Integer spentAmount) {
        int allocated = orZero(allocatedAmount);
        int spent = orZero(spentAmount);
        if (allocated <= 0) {
            return 0.0;
        }
        return Math.round(((double) spent / allocated) * 1000) / 10.0;
    }

    private AiPlanAnalysisRequest.AiTransactionItem toAiPlanTransactionItem(
            Transaction transaction,
            String counterpartyName,
            String category,
            String memo
    ) {
        String transactionDate = transaction.getTransactionAt() != null
                ? transaction.getTransactionAt().toLocalDate().toString()
                : LocalDate.now().toString();
        String txType = normalizeTxType(transaction);

        return new AiPlanAnalysisRequest.AiTransactionItem(
                transaction.getId() != null ? transaction.getId().toString() : null,
                transactionDate,
                transaction.getAmount(),
                counterpartyName,
                category,
                memo,
                txType
        );
    }

    private LocalDate resolvePaydayDate(YearMonth yearMonth, int paydayDay) {
        return yearMonth.atDay(Math.min(Math.max(paydayDay, 1), yearMonth.lengthOfMonth()));
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
                .filter(id -> id != null)
                .toList();

        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        return transactionAiMetadataRepository.findAllByTransactionIdIn(transactionIds).stream()
                .collect(Collectors.toMap(metadata -> metadata.getTransaction().getId(), Function.identity(), (left, right) -> left));
    }

    private String resolvePlanCounterpartyName(Transaction transaction, Payment payment) {
        if (payment != null && payment.getMerchantName() != null && !payment.getMerchantName().isBlank()) {
            return payment.getMerchantName();
        }
        return transaction.getCounterpartyName();
    }

    private String resolvePlanMemo(Transaction transaction, Payment payment) {
        if (payment != null && payment.getMerchantName() != null && !payment.getMerchantName().isBlank()) {
            return payment.getMerchantName();
        }
        return transaction.getTransactionSummary();
    }

    private String resolvePlanCategory(Transaction transaction, TransactionAiMetadata metadata) {
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

        return UNCATEGORIZED;
    }

    private AiSpendingReportGenerateRequest.AiTransactionItem toAiReportTransactionItem(
            Transaction transaction,
            String counterpartyName,
            String category,
            String memo
    ) {
        String transactionDate = transaction.getTransactionAt() != null
                ? transaction.getTransactionAt().toLocalDate().toString()
                : LocalDate.now().toString();
        String txType = normalizeTxType(transaction);

        return new AiSpendingReportGenerateRequest.AiTransactionItem(
                transaction.getId() != null ? transaction.getId().toString() : null,
                transactionDate,
                transaction.getAmount(),
                counterpartyName,
                category,
                memo,
                txType
        );
    }

    private String summarizePlanTransactions(List<AiPlanAnalysisRequest.AiTransactionItem> transactions) {
        if (transactions.isEmpty()) {
            return "[]";
        }

        return transactions.stream()
                .limit(5)
                .map(item -> String.format(
                        "%s/%s/%s",
                        item.category() != null ? item.category() : UNCATEGORIZED,
                        item.type() != null ? item.type() : "UNKNOWN",
                        item.amount() != null ? item.amount() : 0
                ))
                .collect(Collectors.joining(", ", "[", transactions.size() > 5 ? ", ...]" : "]"));
    }

    private boolean isAccountSideTransaction(Transaction transaction, String category) {
        return isAccountSideTransaction(transaction, null, category);
    }

    private boolean isAccountSideTransaction(Transaction transaction, Payment payment, String category) {
        if (payment != null) {
            return false;
        }
        if (isDeposit(transaction)) {
            return true;
        }
        if (category == null || category.isBlank() || UNCATEGORIZED.equals(category)) {
            return true;
        }
        return "금융/보험".equals(category);
    }

    private static boolean isDeposit(Transaction transaction) {
        String txType = transaction.getTxType();
        return txType != null && DEPOSIT_TX_TYPES.contains(txType.toUpperCase(Locale.ROOT));
    }

    // AI 서버에는 저장 경로와 상관없이 'DEPOSIT'/'WITHDRAWAL'로 보낸다. 그 밖의 값은 그대로 둔다.
    private static String normalizeTxType(Transaction transaction) {
        String txType = transaction.getTxType();
        if (txType == null) {
            return "WITHDRAWAL";
        }
        String upper = txType.toUpperCase(Locale.ROOT);
        if (DEPOSIT_TX_TYPES.contains(upper)) {
            return "DEPOSIT";
        }
        if (WITHDRAWAL_TX_TYPES.contains(upper)) {
            return "WITHDRAWAL";
        }
        return txType;
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

    private double orZero(Double value) {
        return value != null ? value : 0.0;
    }
}
