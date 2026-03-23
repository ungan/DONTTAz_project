package com.donttaz.donttazapi.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
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
import com.donttaz.donttazapi.ai.dto.request.AiTransactionSyncRequest;
import com.donttaz.donttazapi.ai.dto.request.SpendingPlanCreateRequest;
import com.donttaz.donttazapi.ai.dto.request.TransactionCategoryUpdateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiBurndownChartResponse;
import com.donttaz.donttazapi.ai.dto.response.AiCategorizedListResponse;
import com.donttaz.donttazapi.ai.dto.response.AiPlanAnalysisResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiTransactionServiceTest {

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

    @InjectMocks
    private AiTransactionService aiTransactionService;

    @Test
    @DisplayName("getBurndownChart calculates remaining budget by day")
    void getBurndownChart_Success() {
        Long userId = 1L;
        String yearMonth = "2026-03";
        int totalBudget = 310000;

        SpendingPlan plan = createTestSpendingPlan(userId, yearMonth, totalBudget);
        Transaction tx1 = Transaction.builder().amount(20000L).transactionAt(LocalDateTime.of(2026, 3, 1, 10, 0)).build();
        Transaction tx2 = Transaction.builder().amount(10000L).transactionAt(LocalDateTime.of(2026, 3, 2, 15, 0)).build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx1, tx2)));

        AiBurndownChartResponse response = aiTransactionService.getBurndownChart(yearMonth);

        assertThat(response.totalBudget()).isEqualTo(totalBudget);
        assertThat(response.chartData()).hasSize(31);
        assertThat(response.chartData().get(0).actualRemaining()).isEqualTo(290000);
        assertThat(response.chartData().get(1).actualRemaining()).isEqualTo(280000);

        verify(spendingPlanRepository, times(1)).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionRepository, times(1)).searchWithAiMetadata(eq(userId), any(), any(), any());
    }

    @Test
    @DisplayName("getSpendingReport aggregates amounts by AI category")
    void getSpendingReport_Success() {
        Long userId = 1L;
        String yearMonth = "2026-02";

        SpendingPlan plan = createTestSpendingPlanWithCategories(userId, yearMonth, 150000);
        Transaction tx1 = Transaction.builder().amount(60000L).build();
        Transaction tx2 = Transaction.builder().amount(30000L).build();
        ReflectionTestUtils.setField(tx1, "id", 101L);
        ReflectionTestUtils.setField(tx2, "id", 102L);

        TransactionAiMetadata m1 = TransactionAiMetadata.builder().aiCategory("Food").build();
        TransactionAiMetadata m2 = TransactionAiMetadata.builder().aiCategory("Transport").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx1, tx2)));
        given(transactionAiMetadataRepository.findByTransactionId(101L)).willReturn(Optional.of(m1));
        given(transactionAiMetadataRepository.findByTransactionId(102L)).willReturn(Optional.of(m2));

        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);

        assertThat(response.totalSpent()).isEqualTo(90000);
        assertThat(response.achievementRate()).isEqualTo(60);
        assertThat(response.categoryReports()).hasSize(2);

        verify(spendingPlanRepository, times(1)).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionAiMetadataRepository, atLeastOnce()).findByTransactionId(anyLong());
    }

    @Test
    @DisplayName("createSpendingPlan builds plan from recent categorized history")
    void createSpendingPlan_Success() {
        Long userId = 1L;
        String yearMonth = "2026-03";
        SpendingPlanCreateRequest request = new SpendingPlanCreateRequest(yearMonth, 3000000);

        Transaction tx = Transaction.builder().userId(userId).amount(500000L).build();
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("Food").build();
        AiPlanAnalysisResponse aiResponse = new AiPlanAnalysisResponse(
                2500000,
                List.of(new AiPlanAnalysisResponse.RecommendedCategoryPlan("Food", 600000, "keep it stable"))
        );

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx)));
        given(transactionAiMetadataRepository.findByTransactionId(any())).willReturn(Optional.of(metadata));
        given(aiServiceClient.calculateSpendingPlan(any(AiPlanAnalysisRequest.class))).willReturn(aiResponse);
        given(spendingPlanRepository.save(any(SpendingPlan.class))).willAnswer(invocation -> {
            SpendingPlan plan = invocation.getArgument(0);
            ReflectionTestUtils.setField(plan, "id", 305L);
            return plan;
        });

        SpendingPlanResponse response = aiTransactionService.createSpendingPlan(request);

        assertThat(response.planId()).isEqualTo(305L);
        assertThat(response.surplusAmount()).isEqualTo(500000);

        verify(spendingPlanRepository, times(1)).deleteByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(spendingPlanRepository, times(1)).save(any(SpendingPlan.class));
    }

    @Test
    @DisplayName("updateTransactionCategory marks metadata as modified")
    void updateTransactionCategory_Success() {
        Long userId = 1L;
        Long transactionId = 5012L;
        TransactionCategoryUpdateRequest request = new TransactionCategoryUpdateRequest("Food");
        Transaction tx = Transaction.builder().userId(userId).counterpartyName("Cafe").build();
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("Uncategorized").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));
        given(transactionAiMetadataRepository.findByTransactionId(transactionId)).willReturn(Optional.of(metadata));

        TransactionCategoryUpdateResponse response = aiTransactionService.updateTransactionCategory(transactionId, request);

        assertThat(response.updatedCategory()).isEqualTo("Food");
        assertThat(response.isModified()).isTrue();

        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("updateTransactionCategory rejects transactions from other users")
    void updateTransactionCategory_AccessDenied() {
        Long userId = 1L;
        Long transactionId = 5012L;
        Transaction tx = Transaction.builder().userId(2L).build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));

        CustomException ex = assertThrows(
                CustomException.class,
                () -> aiTransactionService.updateTransactionCategory(transactionId, new TransactionCategoryUpdateRequest("Food"))
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("syncTransactions analyzes newly synced transactions")
    void syncTransactions_Success() {
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        Transaction syncedTransaction = Transaction.builder().counterpartyName("Salary").build();
        AiAnalysisResponse aiResponse = new AiAnalysisResponse(
                List.of(new AiAnalysisResponse.AiAnalysisResult("Salary", "Income", 0.99))
        );

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(transactionSyncService.syncTransactions(eq(account), any(), any())).willReturn(List.of(syncedTransaction));
        given(aiServiceClient.analyzeMerchants(any())).willReturn(aiResponse);

        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        verify(transactionSyncService, times(1)).syncTransactions(eq(account), any(), any());
        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }

    @Test
    @DisplayName("syncTransactions falls back to uncategorized metadata on AI failure")
    void syncTransactions_AiServerFailure_Fallback() {
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        Transaction syncedTransaction = Transaction.builder().counterpartyName("Starbucks").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(transactionSyncService.syncTransactions(eq(account), any(), any())).willReturn(List.of(syncedTransaction));
        given(aiServiceClient.analyzeMerchants(any())).willThrow(new RuntimeException("Down"));

        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }

    @Test
    @DisplayName("getCategorizedTransactions returns paged items with metadata")
    void getCategorizedTransactions_Success() {
        Long userId = 1L;
        Transaction tx = Transaction.builder()
                .amount(15000L)
                .counterpartyName("Cafe")
                .transactionAt(LocalDateTime.of(2026, 3, 10, 12, 0))
                .build();
        ReflectionTestUtils.setField(tx, "id", 901L);
        TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                .aiCategory("Food")
                .confidenceScore(0.87)
                .build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx), PageRequest.of(0, 20), 1));
        given(transactionAiMetadataRepository.findByTransactionId(901L)).willReturn(Optional.of(metadata));

        AiCategorizedListResponse response = aiTransactionService.getCategorizedTransactions("2026-03", PageRequest.of(0, 20));

        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.categorizedList()).hasSize(1);
        assertThat(response.categorizedList().get(0).aiCategory()).isEqualTo("Food");
    }

    private SpendingPlan createTestSpendingPlan(Long userId, String ym, int budget) {
        return SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(ym)
                .totalBudget(budget)
                .monthlyIncome(3000000)
                .surplusAmount(3000000 - budget)
                .build();
    }

    private SpendingPlan createTestSpendingPlanWithCategories(Long userId, String ym, int budget) {
        SpendingPlan plan = createTestSpendingPlan(userId, ym, budget);
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("Food").allocatedAmount(100000).build());
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("Transport").allocatedAmount(50000).build());
        return plan;
    }
}
