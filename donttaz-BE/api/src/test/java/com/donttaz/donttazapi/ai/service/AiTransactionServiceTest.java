package com.donttaz.donttazapi.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AiTransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAiMetadataRepository transactionAiMetadataRepository;

    @Mock
    private BankingAccountQueryService bankingAccountQueryService;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AiTransactionService aiTransactionService;

    @Test
    @DisplayName("거래 내역의 카테고리를 성공적으로 수정한다.")
    void updateTransactionCategory_Success() {
        // [Given]
        Long userId = 1L;
        Long transactionId = 5012L;
        TransactionCategoryUpdateRequest request = new TransactionCategoryUpdateRequest("식비");
        
        Transaction tx = Transaction.builder()
                .userId(userId)
                .counterpartyName("요기요_치킨")
                .build();
        
        TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                .transaction(tx)
                .aiCategory("편의점")
                .build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));
        given(transactionAiMetadataRepository.findByTransactionId(transactionId)).willReturn(Optional.of(metadata));

        // [When]
        TransactionCategoryUpdateResponse response = aiTransactionService.updateTransactionCategory(transactionId, request);

        // [Then]
        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.previousCategory()).isEqualTo("편의점");
        assertThat(response.updatedCategory()).isEqualTo("식비");
        assertThat(response.isModified()).isTrue();
        assertThat(response.modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자의 거래 내역을 수정하려고 하면 ACCESS_DENIED 예외가 발생한다.")
    void updateTransactionCategory_AccessDenied() {
        // [Given]
        Long userId = 1L;
        Long otherUserId = 2L;
        Long transactionId = 5012L;
        
        Transaction tx = Transaction.builder()
                .userId(otherUserId)
                .build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(tx));

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> {
            aiTransactionService.updateTransactionCategory(transactionId, new TransactionCategoryUpdateRequest("식비"));
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 거래 ID로 수정하려고 하면 TRANSACTION_NOT_FOUND 예외가 발생한다.")
    void updateTransactionCategory_NotFound() {
        // [Given]
        Long userId = 1L;
        Long invalidId = 9999L;

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.findById(invalidId)).willReturn(Optional.empty());

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> {
            aiTransactionService.updateTransactionCategory(invalidId, new TransactionCategoryUpdateRequest("식비"));
        });
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND);
    }

    @Test
    @DisplayName("특정 연월의 거래 내역을 AI 카테고리와 함께 최신순으로 조회한다.")
    void getCategorizedTransactions_Success() {
        // [Given]
        Long userId = 1L;
        String yearMonth = "2026-03";
        Pageable pageable = PageRequest.of(0, 10);
        
        Transaction tx = Transaction.builder()
                .userId(userId)
                .counterpartyName("스타벅스")
                .amount(5000L)
                .transactionAt(LocalDateTime.of(2026, 3, 5, 10, 0))
                .build();
        
        TransactionAiMetadata metadata = TransactionAiMetadata.builder()
                .transaction(tx)
                .aiCategory("카페")
                .confidenceScore(0.95)
                .isModified(false)
                .build();

        Page<Transaction> txPage = new PageImpl<>(List.of(tx), pageable, 1);

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), eq(pageable)))
                .willReturn(txPage);
        given(transactionAiMetadataRepository.findByTransactionId(any())).willReturn(Optional.of(metadata));

        // [When]
        AiCategorizedListResponse response = aiTransactionService.getCategorizedTransactions(yearMonth, pageable);

        // [Then]
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.categorizedList()).hasSize(1);
        assertThat(response.categorizedList().get(0).storeName()).isEqualTo("스타벅스");
        assertThat(response.categorizedList().get(0).aiCategory()).isEqualTo("카페");
        assertThat(response.categorizedList().get(0).confidenceScore()).isEqualTo(0.95);
        assertThat(response.categorizedList().get(0).isModified()).isFalse();
    }

    @Test
    @DisplayName("조회된 거래 내역에 AI 메타데이터가 없으면 '미분류'로 표시한다.")
    void getCategorizedTransactions_NoMetadata_ReturnsDefault() {
        // [Given]
        Long userId = 1L;
        String yearMonth = "2026-03";
        Pageable pageable = PageRequest.of(0, 10);
        
        Transaction tx = Transaction.builder()
                .userId(userId)
                .counterpartyName("알 수 없는 상점")
                .amount(1000L)
                .transactionAt(LocalDateTime.of(2026, 3, 10, 15, 0))
                .build();
        
        Page<Transaction> txPage = new PageImpl<>(List.of(tx), pageable, 1);

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(transactionRepository.searchWithAiMetadata(eq(userId), any(), any(), eq(pageable)))
                .willReturn(txPage);
        given(transactionAiMetadataRepository.findByTransactionId(any())).willReturn(Optional.empty());

        // [When]
        AiCategorizedListResponse response = aiTransactionService.getCategorizedTransactions(yearMonth, pageable);

        // [Then]
        assertThat(response.categorizedList().get(0).aiCategory()).isEqualTo("미분류");
        assertThat(response.categorizedList().get(0).confidenceScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("신규 거래 내역이 있으면 AI 분석을 거쳐 성공적으로 동기화한다.")
    void syncTransactions_Success() {
        // [Given]
        Long userId = 1L;
        String accountNo = "123-456";
        Account mockAccount = Account.builder()
                .userId(userId)
                .accountNumber(accountNo)
                .build();
        
        AiTransactionSyncRequest request = new AiTransactionSyncRequest(LocalDate.now().minusDays(7), LocalDate.now());
        
        TransactionSummaryResult txResult = new TransactionSummaryResult(
                "FIN_UNIQUE_001", LocalDateTime.now(), "입금", 10000, 110000, "급여", "기타"
        );
        TransactionQueryResult queryResult = new TransactionQueryResult(1, 1, false, List.of(txResult));
        
        Transaction savedTx = Transaction.builder()
                .counterpartyName("급여")
                .build();

        AiAnalysisResponse.AiAnalysisResult aiResult = new AiAnalysisResponse.AiAnalysisResult("급여", "기타", 0.99);
        AiAnalysisResponse aiResponse = new AiAnalysisResponse(List.of(aiResult));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(mockAccount));
        given(bankingAccountQueryService.getTransactions(eq(accountNo), any(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(queryResult);
        given(transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(any(), anyString(), anyString()))
                .willReturn(false);
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTx);
        given(aiServiceClient.analyzeMerchants(any())).willReturn(aiResponse);

        // [When]
        AiTransactionSyncResponse response = aiTransactionService.syncTransactions(request);

        // [Then]
        assertThat(response.syncedCount()).isEqualTo(1);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(aiServiceClient, times(1)).analyzeMerchants(any());
        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }

    @Test
    @DisplayName("이미 존재하는 거래 내역은 다시 저장하지 않는다.")
    void syncTransactions_DuplicateIgnored() {
        // [Given]
        Long userId = 1L;
        String accountNo = "123-456";
        Account mockAccount = Account.builder().userId(userId).accountNumber(accountNo).build();
        
        TransactionSummaryResult txResult = new TransactionSummaryResult(
                "DUPLICATE_ID", LocalDateTime.now(), "입금", 10000, 110000, "급여", "기타"
        );
        TransactionQueryResult queryResult = new TransactionQueryResult(1, 1, false, List.of(txResult));

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(mockAccount));
        given(bankingAccountQueryService.getTransactions(anyString(), any(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(queryResult);
        given(transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(any(), anyString(), eq("DUPLICATE_ID")))
                .willReturn(true);

        // [When]
        AiTransactionSyncResponse response = aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // [Then]
        assertThat(response.syncedCount()).isEqualTo(0);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(aiServiceClient, never()).analyzeMerchants(any());
    }

    @Test
    @DisplayName("AI 서버에 오류가 발생해도 거래 내역은 저장되고 '미분류'로 메타데이터가 생성된다.")
    void syncTransactions_AiServerFailure_Fallback() {
        // [Given]
        Long userId = 1L;
        Account mockAccount = Account.builder().userId(userId).accountNumber("123").build();
        
        TransactionSummaryResult txResult = new TransactionSummaryResult(
                "TX_001", LocalDateTime.now(), "입금", 5000, 95000, "스타벅스", "카페"
        );
        TransactionQueryResult queryResult = new TransactionQueryResult(1, 1, false, List.of(txResult));
        Transaction savedTx = Transaction.builder().counterpartyName("스타벅스").build();

        given(currentUserProvider.getCurrentUserId()).willReturn(userId);
        given(accountRepository.findByUserId(userId)).willReturn(List.of(mockAccount));
        given(bankingAccountQueryService.getTransactions(anyString(), any(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(queryResult);
        given(transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(any(), anyString(), anyString()))
                .willReturn(false);
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTx);
        
        given(aiServiceClient.analyzeMerchants(any())).willThrow(new RuntimeException("AI Server Down"));

        // [When]
        AiTransactionSyncResponse response = aiTransactionService.syncTransactions(new AiTransactionSyncRequest(null, null));

        // [Then]
        assertThat(response.syncedCount()).isEqualTo(1);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionAiMetadataRepository, times(1)).save(any(TransactionAiMetadata.class));
    }
}
