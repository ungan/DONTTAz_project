package com.donttaz.donttazapi.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.TransactionSyncService;
import com.donttaz.donttazapi.ai.domain.CategorySpendingPlan;
import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiTransactionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAiMetadataRepository transactionAiMetadataRepository;

    @Mock
    private SpendingPlanRepository spendingPlanRepository;

    @Mock
    private TransactionSyncService transactionSyncService;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private Environment environment;

    @InjectMocks
    private AiTransactionService aiTransactionService;

    @Test
    @DisplayName("예산 기준 번다운 차트를 계산해 반환한다.")
    void getBurndownChart_Success() {
        Long userId = 1L;
        String yearMonth = "2026-03";
        int totalBudget = 310000;

        // Given
        SpendingPlan plan = createTestSpendingPlan(userId, yearMonth, totalBudget);
        Transaction tx1 = Transaction.builder()
                .amount(20000L)
                .transactionAt(LocalDateTime.of(2026, 3, 1, 10, 0))
                .build();
        Transaction tx2 = Transaction.builder()
                .amount(10000L)
                .transactionAt(LocalDateTime.of(2026, 3, 2, 15, 0))
                .build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx1, tx2)));

        // When
        AiBurndownChartResponse response = aiTransactionService.getBurndownChart(yearMonth);

        // Then
        assertThat(response.totalBudget()).isEqualTo(totalBudget);
        assertThat(response.chartData()).hasSize(31);
        assertThat(response.chartData().get(0).actualRemaining()).isEqualTo(290000);
        assertThat(response.chartData().get(1).actualRemaining()).isEqualTo(280000);
        verify(spendingPlanRepository).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionRepository).searchWithAiMetadata(eq(userId), any(), any(), any());
    }

    @Test
    @DisplayName("local/dev 환경에서 계획이 없으면 더미 번다운 데이터를 반환한다.")
    void getBurndownChart_LocalProfileFallback() {
        Long userId = 1L;
        String yearMonth = "2026-03";

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.empty());
        given(environment.acceptsProfiles(any(Profiles.class))).willReturn(true);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));

        // When
        AiBurndownChartResponse response = aiTransactionService.getBurndownChart(yearMonth);

        // Then
        assertThat(response.targetYearMonth()).isEqualTo(yearMonth);
        assertThat(response.totalBudget()).isEqualTo(2500000);
        assertThat(response.chartData()).hasSize(31);
        verify(environment).acceptsProfiles(any(Profiles.class));
    }

    @Test
    @DisplayName("local/dev 가 아니고 계획이 없으면 INVALID_REQUEST 예외를 던진다.")
    void getBurndownChart_NoPlanOutsideLocal_ThrowsInvalidRequest() {
        Long userId = 1L;
        String yearMonth = "2026-03";

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.empty());
        given(environment.acceptsProfiles(any(Profiles.class))).willReturn(false);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> aiTransactionService.getBurndownChart(yearMonth));

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(transactionRepository, never()).searchWithAiMetadata(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("지출 리포트를 생성해 반환한다.")
    void getSpendingReport_Success() {
        Long userId = 1L;
        String yearMonth = "2026-02";

        // Given
        SpendingPlan plan = createTestSpendingPlanWithCategories(userId, yearMonth, 150000);
        Transaction tx1 = Transaction.builder().amount(60000L).build();
        Transaction tx2 = Transaction.builder().amount(30000L).build();
        ReflectionTestUtils.setField(tx1, "id", 101L);
        ReflectionTestUtils.setField(tx2, "id", 102L);

        TransactionAiMetadata m1 = TransactionAiMetadata.builder().aiCategory("식비").build();
        TransactionAiMetadata m2 = TransactionAiMetadata.builder().aiCategory("교통").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx1, tx2)));
        given(transactionAiMetadataRepository.findByTransactionId(101L)).willReturn(Optional.of(m1));
        given(transactionAiMetadataRepository.findByTransactionId(102L)).willReturn(Optional.of(m2));

        // When
        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);

        // Then
        assertThat(response.totalSpent()).isEqualTo(90000);
        assertThat(response.achievementRate()).isEqualTo(60);
        assertThat(response.categoryReports()).hasSize(2);
        verify(spendingPlanRepository).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionAiMetadataRepository, atLeastOnce()).findByTransactionId(anyLong());
    }

    @Test
    @DisplayName("최근 3개월 내역으로 확장 소비 계획을 생성한다.")
    void createSpendingPlan_Success() {
        Long userId = 1L;
        String yearMonth = "2026-03";
        SpendingPlanCreateRequest request = new SpendingPlanCreateRequest(yearMonth, 3000000);

        // Given
        Transaction transaction = createSavedTransaction(
                201L,
                userId,
                500000L,
                LocalDateTime.of(2026, 2, 10, 9, 0),
                "WITHDRAWAL",
                "마트"
        );
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("식비").build();
        User user = User.builder().salaryDay(25).build();
        AiPlanAnalysisResponse aiResponse = new AiPlanAnalysisResponse(
                "SUCCESS",
                null,
                new AiPlanAnalysisResponse.AiPlanData(
                        yearMonth,
                        "2025-12",
                        "2026-02",
                        2500000,
                        750000,
                        500000,
                        25,
                        0.32,
                        List.of(new AiPlanAnalysisResponse.RecommendedCategoryPlan("식비", 600000, "예산을 유지하세요")),
                        "safe",
                        List.of(new AiPlanAnalysisResponse.PersonalizedRecommendation("saving_tip", null, "봉인 금액을 유지해보세요.")),
                        "균형소비형"
                )
        );

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(transaction)));
        given(transactionAiMetadataRepository.findByTransactionId(transaction.getId())).willReturn(Optional.of(metadata));
        given(accountRepository.findByUserId(userId))
                .willReturn(List.of(Account.builder().userId(userId).balance(1500000L).build()));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(aiServiceClient.calculateSpendingPlan(any(AiPlanAnalysisRequest.class))).willReturn(aiResponse);
        given(spendingPlanRepository.save(any(SpendingPlan.class))).willAnswer(invocation -> {
            SpendingPlan plan = invocation.getArgument(0);
            ReflectionTestUtils.setField(plan, "id", 305L);
            return plan;
        });

        // When
        SpendingPlanResponse response = aiTransactionService.createSpendingPlan(request);

        // Then
        assertThat(response.planId()).isEqualTo(305L);
        assertThat(response.referenceStartMonth()).isEqualTo("2025-12");
        assertThat(response.referenceEndMonth()).isEqualTo("2026-02");
        assertThat(response.sealedAmount()).isEqualTo(750000);
        assertThat(response.surplusAmount()).isEqualTo(500000);
        assertThat(response.budgetStrategy()).isEqualTo("safe");
        assertThat(response.personalizedRecommendations()).hasSize(1);
        assertThat(response.spendingPersona()).isEqualTo("균형소비형");
        verify(spendingPlanRepository).deleteByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(aiServiceClient).calculateSpendingPlan(any(AiPlanAnalysisRequest.class));
        verify(spendingPlanRepository).save(any(SpendingPlan.class));
    }

    @Test
    @DisplayName("거래 카테고리를 수정한다.")
    void updateTransactionCategory_Success() {
        Long userId = 1L;
        Long transactionId = 5012L;
        TransactionCategoryUpdateRequest request = new TransactionCategoryUpdateRequest("식비");
        Transaction transaction = Transaction.builder().userId(userId).counterpartyName("치킨집").build();
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("편의점").build();

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(transaction));
        given(transactionAiMetadataRepository.findByTransactionId(transactionId)).willReturn(Optional.of(metadata));

        // When
        TransactionCategoryUpdateResponse response = aiTransactionService.updateTransactionCategory(transactionId, request);

        // Then
        assertThat(response.updatedCategory()).isEqualTo("식비");
        assertThat(response.isModified()).isTrue();
        verify(transactionRepository).findById(transactionId);
        verify(transactionAiMetadataRepository).findByTransactionId(transactionId);
    }

    @Test
    @DisplayName("다른 사용자의 거래를 수정하려 하면 ACCESS_DENIED 예외가 발생한다.")
    void updateTransactionCategory_AccessDenied() {
        Long userId = 1L;
        Long transactionId = 5012L;
        Transaction transaction = Transaction.builder().userId(2L).build();

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(transaction));

        // When
        CustomException exception = assertThrows(
                CustomException.class,
                () -> aiTransactionService.updateTransactionCategory(transactionId, new TransactionCategoryUpdateRequest("식비"))
        );

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    @DisplayName("신규 거래를 동기화하면 AI 메타데이터를 저장한다.")
    void syncTransactions_Success() {
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        Transaction syncedTransaction = createSavedTransaction(
                301L,
                userId,
                10000L,
                LocalDateTime.now(),
                "DEPOSIT",
                "급여"
        );
        AiAnalysisResponse aiResponse = new AiAnalysisResponse(List.of(
                new AiAnalysisResponse.AiAnalysisResult(
                        syncedTransaction.getId().toString(),
                        "급여",
                        "기타",
                        0.99
                )
        ));

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(transactionSyncService.syncTransactions(eq(account), any(), any())).willReturn(List.of(syncedTransaction));
        given(transactionRepository.findByUserId(userId)).willReturn(List.of());
        given(aiServiceClient.analyzeTransactions(any())).willReturn(aiResponse);
        given(transactionAiMetadataRepository.findByTransactionId(301L)).willReturn(Optional.empty());

        // When
        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // Then
        verify(transactionSyncService, times(1)).syncTransactions(eq(account), any(), any());
        verify(transactionAiMetadataRepository, atLeastOnce()).save(any(TransactionAiMetadata.class));
        verify(aiServiceClient).analyzeTransactions(any());
    }

    @Test
    @DisplayName("AI 서버 실패 시에도 미분류 메타데이터를 저장한다.")
    void syncTransactions_AiServerFailure_Fallback() {
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        Transaction syncedTransaction = createSavedTransaction(
                302L,
                userId,
                5000L,
                LocalDateTime.now(),
                "WITHDRAWAL",
                "스타벅스"
        );

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(userRepository.findById(userId)).willReturn(Optional.empty());
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(transactionSyncService.syncTransactions(eq(account), any(), any())).willReturn(List.of(syncedTransaction));
        given(transactionRepository.findByUserId(userId)).willReturn(List.of());
        given(aiServiceClient.analyzeTransactions(any())).willThrow(new RuntimeException("Down"));
        given(transactionAiMetadataRepository.findByTransactionId(302L)).willReturn(Optional.empty());

        // When
        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // Then
        verify(transactionAiMetadataRepository, atLeastOnce()).save(any(TransactionAiMetadata.class));
        verify(aiServiceClient).analyzeTransactions(any());
    }

    @Test
    @DisplayName("저장된 거래를 재분석한다.")
    void reanalyzeTransactions_Success() {
        Long userId = 1L;
        Transaction transaction = createSavedTransaction(
                401L,
                userId,
                12000L,
                LocalDateTime.of(2026, 3, 1, 12, 0),
                "WITHDRAWAL",
                "카페"
        );
        AiAnalysisResponse aiResponse = new AiAnalysisResponse(List.of(
                new AiAnalysisResponse.AiAnalysisResult(
                        transaction.getId().toString(),
                        "카페",
                        "식비",
                        0.91
                )
        ));

        // Given
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(transaction)));
        given(transactionAiMetadataRepository.findByTransactionId(401L)).willReturn(Optional.empty());
        given(aiServiceClient.analyzeTransactions(any())).willReturn(aiResponse);

        // When
        AiTransactionReanalyzeResponse response = aiTransactionService.reanalyzeTransactions(
                new AiTransactionReanalyzeRequest(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), false)
        );

        // Then
        assertThat(response.matchedTransactionCount()).isEqualTo(1);
        assertThat(response.reanalyzedCount()).isEqualTo(1);
        verify(transactionAiMetadataRepository, atLeastOnce()).save(any(TransactionAiMetadata.class));
        verify(aiServiceClient).analyzeTransactions(any());
    }

    @Test
    @DisplayName("카테고리 결과 조회는 메타데이터를 포함해 반환한다.")
    void getCategorizedTransactions_Success() {
        Long userId = 1L;

        // Given
        Transaction transaction = Transaction.builder()
                .amount(15000L)
                .counterpartyName("Cafe")
                .transactionAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .build();
        ReflectionTestUtils.setField(transaction, "id", 901L);
        TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                .aiCategory("Food")
                .confidenceScore(0.87)
                .build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(transaction), PageRequest.of(0, 20), 1));
        given(transactionAiMetadataRepository.findByTransactionId(901L)).willReturn(Optional.of(metadata));

        // When
        AiCategorizedListResponse response = aiTransactionService.getCategorizedTransactions("2026-03", PageRequest.of(0, 20));

        // Then
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.categorizedList()).hasSize(1);
        assertThat(response.categorizedList().get(0).aiCategory()).isEqualTo("Food");
        verify(transactionRepository).searchWithAiMetadata(eq(userId), any(), any(), any());
        verify(transactionAiMetadataRepository).findByTransactionId(901L);
    }

    private SpendingPlan createTestSpendingPlan(Long userId, String yearMonth, int budget) {
        return SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(yearMonth)
                .totalBudget(budget)
                .monthlyIncome(3000000)
                .surplusAmount(3000000 - budget)
                .sealedAmount(0)
                .build();
    }

    private SpendingPlan createTestSpendingPlanWithCategories(Long userId, String yearMonth, int budget) {
        SpendingPlan plan = createTestSpendingPlan(userId, yearMonth, budget);
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("식비").allocatedAmount(100000).build());
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("교통").allocatedAmount(50000).build());
        return plan;
    }

    private Transaction createSavedTransaction(
            Long id,
            Long userId,
            Long amount,
            LocalDateTime transactionAt,
            String txType,
            String counterpartyName
    ) {
        Transaction transaction = Transaction.builder()
                .account(Account.builder().userId(userId).accountNumber("123-456").build())
                .userId(userId)
                .source("FIN_API")
                .sourceTxId("SRC_" + id)
                .txType(txType)
                .txTypeName(txType)
                .amount(amount)
                .balanceSnapshot(0L)
                .counterpartyName(counterpartyName)
                .transactionSummary(counterpartyName)
                .transactionAt(transactionAt)
                .syncedAt(transactionAt)
                .build();
        ReflectionTestUtils.setField(transaction, "id", id);
        return transaction;
    }
}
