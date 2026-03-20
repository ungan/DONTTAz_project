package com.donttaz.donttazapi.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.donttaz.donttazapi.account.service.BankingAccountQueryService;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
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
import com.donttaz.donttazapi.ai.dto.response.AiTransactionSyncResponse;
import com.donttaz.donttazapi.ai.dto.response.SpendingPlanResponse;
import com.donttaz.donttazapi.ai.dto.response.TransactionCategoryUpdateResponse;
import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private BankingAccountQueryService bankingAccountQueryService;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AiTransactionService aiTransactionService;

    @Test
    @DisplayName("특정 월의 생존 번다운 차트 데이터를 성공적으로 계산하여 반환한다.")
    void getBurndownChart_Success() {
        // [Given]
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

        // [When]
        AiBurndownChartResponse response = aiTransactionService.getBurndownChart(yearMonth);

        // [Then]
        assertThat(response.totalBudget()).isEqualTo(totalBudget);
        assertThat(response.chartData()).hasSize(31);
        assertThat(response.chartData().get(0).actualRemaining()).isEqualTo(290000);
        assertThat(response.chartData().get(1).actualRemaining()).isEqualTo(280000);
        
        verify(spendingPlanRepository, times(1)).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionRepository, times(1)).searchWithAiMetadata(eq(userId), any(), any(), any());
    }

    @Test
    @DisplayName("특정 월의 소비 리포트를 성공적으로 생성하여 반환한다.")
    void getSpendingReport_Success() {
        // [Given]
        Long userId = 1L;
        String yearMonth = "2026-02";
        
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

        // [When]
        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);

        // [Then]
        assertThat(response.totalSpent()).isEqualTo(90000);
        assertThat(response.achievementRate()).isEqualTo(60);
        assertThat(response.categoryReports()).hasSize(2);
        
        verify(spendingPlanRepository, times(1)).findByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(transactionAiMetadataRepository, atLeastOnce()).findByTransactionId(anyLong());
    }

    @Test
    @DisplayName("최근 3개월 소비 내역을 분석하여 맞춤 소비 플랜을 성공적으로 생성한다.")
    void createSpendingPlan_Success() {
        // [Given]
        Long userId = 1L;
        String yearMonth = "2026-03";
        SpendingPlanCreateRequest request = new SpendingPlanCreateRequest(yearMonth, 3000000);
        
        Transaction tx = Transaction.builder().userId(userId).amount(500000L).build();
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("식비").build();
        given(transactionAiMetadataRepository.findByTransactionId(any())).willReturn(Optional.of(metadata));
        
        AiPlanAnalysisResponse aiResponse = new AiPlanAnalysisResponse(2500000, List.of(
                new AiPlanAnalysisResponse.RecommendedCategoryPlan("식비", 600000, "절약하세요")
        ));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx)));
        given(aiServiceClient.calculateSpendingPlan(any(AiPlanAnalysisRequest.class))).willReturn(aiResponse);
        given(spendingPlanRepository.save(any(SpendingPlan.class))).willAnswer(inv -> {
            SpendingPlan p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 305L);
            return p;
        });

        // [When]
        SpendingPlanResponse response = aiTransactionService.createSpendingPlan(request);

        // [Then]
        assertThat(response.planId()).isEqualTo(305L);
        assertThat(response.surplusAmount()).isEqualTo(500000);
        
        verify(spendingPlanRepository, times(1)).deleteByUserIdAndTargetYearMonth(userId, yearMonth);
        verify(spendingPlanRepository, times(1)).save(any(SpendingPlan.class));
    }

    @Test
    @DisplayName("거래 내역의 카테고리를 성공적으로 수정한다.")
    void updateTransactionCategory_Success() {
        // [Given]
        Long userId = 1L;
        Long transactionId = 5012L;
        TransactionCategoryUpdateRequest request = new TransactionCategoryUpdateRequest("식비");
        Transaction tx = Transaction.builder().userId(userId).counterpartyName("치킨").build();
        TransactionAiMetadata metadata = TransactionAiMetadata.builder().aiCategory("편의점").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));
        given(transactionAiMetadataRepository.findByTransactionId(transactionId)).willReturn(Optional.of(metadata));

        // [When]
        TransactionCategoryUpdateResponse response = aiTransactionService.updateTransactionCategory(transactionId, request);

        // [Then]
        assertThat(response.updatedCategory()).isEqualTo("식비");
        assertThat(response.isModified()).isTrue();
        
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("다른 사용자의 거래 내역을 수정하려고 하면 ACCESS_DENIED 예외가 발생한다.")
    void updateTransactionCategory_AccessDenied() {
        // [Given]
        Long userId = 1L;
        Long transactionId = 5012L;
        Transaction tx = Transaction.builder().userId(2L).build(); // Other user

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));

        // [When & Then]
        CustomException ex = assertThrows(CustomException.class, () -> 
            aiTransactionService.updateTransactionCategory(transactionId, new TransactionCategoryUpdateRequest("식비")));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("신규 거래 내역이 있으면 AI 분석을 거쳐 성공적으로 동기화한다.")
    void syncTransactions_Success() {
        // [Given]
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        TransactionSummaryResult txResult = new TransactionSummaryResult("TX_001", LocalDateTime.now(), "입금", 10000, 110000, "급여", "기타");
        TransactionQueryResult queryResult = new TransactionQueryResult(1, 1, false, List.of(txResult));
        AiAnalysisResponse aiResponse = new AiAnalysisResponse(List.of(new AiAnalysisResponse.AiAnalysisResult("급여", "기타", 0.99)));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(bankingAccountQueryService.getTransactions(anyString(), any(), any(), anyString(), anyInt(), anyInt())).willReturn(queryResult);
        given(transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(any(), anyString(), anyString())).willReturn(false);
        given(transactionRepository.save(any(Transaction.class))).willReturn(Transaction.builder().counterpartyName("급여").build());
        given(aiServiceClient.analyzeMerchants(any())).willReturn(aiResponse);

        // [When]
        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // [Then]
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }

    @Test
    @DisplayName("AI 서버에 오류가 발생해도 거래 내역은 저장되고 '미분류'로 메타데이터가 생성된다.")
    void syncTransactions_AiServerFailure_Fallback() {
        // [Given]
        Long userId = 1L;
        Account account = Account.builder().userId(userId).accountNumber("123").build();
        TransactionSummaryResult txResult = new TransactionSummaryResult("TX_001", LocalDateTime.now(), "입금", 5000, 95000, "스타벅스", "카페");
        TransactionQueryResult queryResult = new TransactionQueryResult(1, 1, false, List.of(txResult));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(account));
        given(bankingAccountQueryService.getTransactions(anyString(), any(), any(), anyString(), anyInt(), anyInt())).willReturn(queryResult);
        given(transactionRepository.save(any(Transaction.class))).willReturn(Transaction.builder().counterpartyName("스타벅스").build());
        given(aiServiceClient.analyzeMerchants(any())).willThrow(new RuntimeException("Down"));

        // [When]
        aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // [Then]
        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }

    // --- Helper Methods ---

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
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("식비").allocatedAmount(100000).build());
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("교통").allocatedAmount(50000).build());
        return plan;
    }
}
