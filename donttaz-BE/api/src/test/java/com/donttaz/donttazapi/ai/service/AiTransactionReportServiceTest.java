package com.donttaz.donttazapi.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.TransactionSyncService;
import com.donttaz.donttazapi.ai.domain.CategorySpendingPlan;
import com.donttaz.donttazapi.ai.domain.SpendingPlan;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import com.donttaz.donttazapi.ai.dto.request.AiSpendingReportGenerateRequest;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportGenerateResponse;
import com.donttaz.donttazapi.ai.dto.response.AiSpendingReportResponse;
import com.donttaz.donttazapi.ai.repository.SpendingPlanRepository;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiTransactionReportServiceTest {

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

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AiTransactionService aiTransactionService;

    @Test
    @DisplayName("AI 리포트 응답을 공개 API 계약으로 매핑한다.")
    void getSpendingReport_MapsAiResponse() {
        // Given
        Long userId = 1L;
        String yearMonth = "2026-03";
        SpendingPlan plan = SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(yearMonth)
                .monthlyIncome(3_000_000)
                .totalBudget(150000)
                .surplusAmount(10000)
                .sealedAmount(20000)
                .build();
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("Food").allocatedAmount(100000).build());

        Transaction tx1 = createTransaction(11L, 60000L, "WITHDRAWAL", "Food Store", LocalDateTime.of(2026, 3, 5, 12, 0));
        Transaction tx2 = createTransaction(12L, 50000L, "WITHDRAWAL", "Etc Store", LocalDateTime.of(2026, 3, 6, 18, 0));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().salaryDay(25).build()));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx1, tx2)));
        given(transactionAiMetadataRepository.findAllByTransactionIdIn(List.of(11L, 12L)))
                .willReturn(List.of(
                        metadataForTransaction(tx1, "Food"),
                        metadataForTransaction(tx2, "Etc")
                ));
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(eq(userId), any()))
                .willReturn(List.of());
        given(aiServiceClient.calculateSpendingReport(any(AiSpendingReportGenerateRequest.class)))
                .willReturn(new AiSpendingReportGenerateResponse(
                        "SUCCESS",
                        "ok",
                        new AiSpendingReportGenerateResponse.ReportData(
                                yearMonth,
                                20000,
                                150000,
                                110000,
                                73.3,
                                "planner",
                                "overall",
                                List.of(
                                        new AiSpendingReportGenerateResponse.CategoryReportItem("Etc", 0, 50000, 45.5, null, "unplanned"),
                                        new AiSpendingReportGenerateResponse.CategoryReportItem("Food", 100000, 60000, 54.5, 60.0, "good")
                                ),
                                List.of(new AiSpendingReportGenerateResponse.PersonalizedFeedbackItem("warning", "Etc", "check"))
                        )
                ));

        // When
        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);

        // Then
        assertThat(response.sealedAmount()).isEqualTo(20000);
        assertThat(response.totalSpent()).isEqualTo(110000);
        assertThat(response.achievementRate()).isEqualTo(73.3);
        assertThat(response.spendingPersona()).isEqualTo("planner");
        assertThat(response.categoryReports()).extracting(AiSpendingReportResponse.CategoryReportItem::categoryName)
                .containsExactly("Food", "Etc");
        assertThat(response.categoryReports().get(1).budgetAchievementRate()).isEqualTo(0.0);
        assertThat(response.personalizedFeedback()).hasSize(1);
    }

    @Test
    @DisplayName("플랜이 없는 월에는 AI fallback 리포트 요청을 보낸다.")
    void getSpendingReport_NoPlan_SendsNullPlan() {
        // Given
        Long userId = 1L;
        String yearMonth = "2026-03";
        Transaction tx = createTransaction(21L, 40000L, "WITHDRAWAL", "Food Store", LocalDateTime.of(2026, 3, 7, 9, 0));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().salaryDay(15).build()));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(tx)));
        given(transactionAiMetadataRepository.findAllByTransactionIdIn(List.of(21L)))
                .willReturn(List.of(metadataForTransaction(tx, "Food")));
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(eq(userId), any()))
                .willReturn(List.of());
        given(aiServiceClient.calculateSpendingReport(any(AiSpendingReportGenerateRequest.class)))
                .willReturn(new AiSpendingReportGenerateResponse(
                        "SUCCESS",
                        "ok",
                        new AiSpendingReportGenerateResponse.ReportData(
                                yearMonth,
                                0,
                                120000,
                                40000,
                                33.3,
                                "fallback",
                                "overall",
                                List.of(new AiSpendingReportGenerateResponse.CategoryReportItem("Food", 0, 40000, 100.0, null, "unplanned")),
                                List.of()
                        )
                ));

        // When
        aiTransactionService.getSpendingReport(yearMonth);

        // Then
        ArgumentCaptor<AiSpendingReportGenerateRequest> captor =
                ArgumentCaptor.forClass(AiSpendingReportGenerateRequest.class);
        verify(aiServiceClient).calculateSpendingReport(captor.capture());
        assertThat(captor.getValue().plan()).isNull();
        assertThat(captor.getValue().paydayDay()).isEqualTo(15);
        assertThat(captor.getValue().cardTransactions()).hasSize(1);
    }

    @Test
    @DisplayName("이체 경로의 입금('1')도 입금으로 보고 계좌 쪽으로 분류하며, AI에는 DEPOSIT/WITHDRAWAL로 보낸다.")
    void getSpendingReport_TransferCodedTypes_AreNormalized() {
        // Given
        Long userId = 1L;
        String yearMonth = "2026-03";
        Transaction transferIn = createTransaction(31L, 500000L, "1", "Transfer In", LocalDateTime.of(2026, 3, 10, 9, 0));
        Transaction transferOut = createTransaction(32L, 40000L, "2", "Food Store", LocalDateTime.of(2026, 3, 11, 12, 0));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.empty());
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().salaryDay(25).build()));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(transferIn, transferOut)));
        given(transactionAiMetadataRepository.findAllByTransactionIdIn(List.of(31L, 32L)))
                .willReturn(List.of(
                        metadataForTransaction(transferIn, "Food"),
                        metadataForTransaction(transferOut, "Food")
                ));
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(eq(userId), any()))
                .willReturn(List.of());
        given(aiServiceClient.calculateSpendingReport(any(AiSpendingReportGenerateRequest.class)))
                .willReturn(new AiSpendingReportGenerateResponse(
                        "SUCCESS",
                        "ok",
                        new AiSpendingReportGenerateResponse.ReportData(
                                yearMonth,
                                0,
                                120000,
                                40000,
                                33.3,
                                "fallback",
                                "overall",
                                List.of(new AiSpendingReportGenerateResponse.CategoryReportItem("Food", 0, 40000, 100.0, null, "unplanned")),
                                List.of()
                        )
                ));

        // When
        aiTransactionService.getSpendingReport(yearMonth);

        // Then
        ArgumentCaptor<AiSpendingReportGenerateRequest> captor =
                ArgumentCaptor.forClass(AiSpendingReportGenerateRequest.class);
        verify(aiServiceClient).calculateSpendingReport(captor.capture());
        assertThat(captor.getValue().accountTransactions())
                .extracting(AiSpendingReportGenerateRequest.AiTransactionItem::id,
                        AiSpendingReportGenerateRequest.AiTransactionItem::type)
                .containsExactly(tuple("31", "DEPOSIT"));
        assertThat(captor.getValue().cardTransactions())
                .extracting(AiSpendingReportGenerateRequest.AiTransactionItem::id,
                        AiSpendingReportGenerateRequest.AiTransactionItem::type)
                .containsExactly(tuple("32", "WITHDRAWAL"));
    }

    @Test
    @DisplayName("AI가 실패하면 fallback 리포트는 입금('DEPOSIT', '1')을 지출에서 빼고 계산한다.")
    void getSpendingReport_AiFails_FallbackExcludesAllDepositCodes() {
        // Given
        Long userId = 1L;
        String yearMonth = "2026-03";
        SpendingPlan plan = SpendingPlan.builder()
                .userId(userId)
                .targetYearMonth(yearMonth)
                .monthlyIncome(3_000_000)
                .totalBudget(150000)
                .surplusAmount(10000)
                .sealedAmount(20000)
                .build();
        plan.addCategoryPlan(CategorySpendingPlan.builder().categoryName("Food").allocatedAmount(100000).build());

        Transaction salary = createTransaction(41L, 3_000_000L, "DEPOSIT", "Salary", LocalDateTime.of(2026, 3, 25, 9, 0));
        Transaction transferIn = createTransaction(42L, 50000L, "1", "Transfer In", LocalDateTime.of(2026, 3, 12, 9, 0));
        Transaction food = createTransaction(43L, 40000L, "WITHDRAWAL", "Food Store", LocalDateTime.of(2026, 3, 13, 12, 0));
        TransactionAiMetadata foodMetadata = metadataForTransaction(food, "Food");

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(spendingPlanRepository.findByUserIdAndTargetYearMonth(userId, yearMonth)).willReturn(Optional.of(plan));
        given(userRepository.findById(userId)).willReturn(Optional.of(User.builder().salaryDay(25).build()));
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(salary, transferIn, food)));
        given(transactionAiMetadataRepository.findAllByTransactionIdIn(List.of(41L, 42L, 43L)))
                .willReturn(List.of(foodMetadata));
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(eq(userId), any()))
                .willReturn(List.of());
        given(aiServiceClient.calculateSpendingReport(any(AiSpendingReportGenerateRequest.class)))
                .willThrow(new RuntimeException("AI server down"));
        given(transactionAiMetadataRepository.findByTransactionId(43L)).willReturn(Optional.of(foodMetadata));

        // When
        AiSpendingReportResponse response = aiTransactionService.getSpendingReport(yearMonth);

        // Then
        assertThat(response.totalSpent()).isEqualTo(40000);
        assertThat(response.categoryReports())
                .extracting(AiSpendingReportResponse.CategoryReportItem::categoryName)
                .containsExactly("Food");
        verify(transactionAiMetadataRepository, never()).findByTransactionId(41L);
        verify(transactionAiMetadataRepository, never()).findByTransactionId(42L);
    }

    private TransactionAiMetadata metadataForTransaction(Transaction transaction, String category) {
        TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                .aiCategory(category)
                .build();
        ReflectionTestUtils.setField(metadata, "transaction", transaction);
        return metadata;
    }

    private Transaction createTransaction(
            Long id,
            Long amount,
            String txType,
            String counterpartyName,
            LocalDateTime transactionAt
    ) {
        Transaction transaction = Transaction.builder()
                .userId(1L)
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
