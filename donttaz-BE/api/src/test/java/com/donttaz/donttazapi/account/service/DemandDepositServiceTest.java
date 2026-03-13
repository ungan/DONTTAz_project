package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandDepositServiceTest {

    @Mock
    private BankingClient bankingClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private DemandDepositService demandDepositService;

    @Test
    @DisplayName("상품 등록 요청을 외부 클라이언트에 위임한다.")
    void createProductDelegatesToBankingClient() {
        // Given
        DemandDepositProductResult expected = new DemandDepositProductResult(
                "001-1-abc",
                "001",
                "한국은행",
                "1",
                "수시입출금",
                "상품명",
                "상품설명",
                "DOMESTIC"
        );
        given(bankingClient.createDemandDepositProduct("001", "상품명", "상품설명")).willReturn(expected);

        // When
        DemandDepositProductResult result = demandDepositService.createProduct("001", "상품명", "상품설명");

        // Then
        assertThat(result).isEqualTo(expected);
        verify(bankingClient).createDemandDepositProduct("001", "상품명", "상품설명");
    }

    @Test
    @DisplayName("상품 목록 조회 요청을 외부 클라이언트에 위임한다.")
    void getProductsDelegatesToBankingClient() {
        // Given
        List<DemandDepositProductResult> expected = List.of(
                new DemandDepositProductResult(
                        "001-1-abc",
                        "001",
                        "한국은행",
                        "1",
                        "수시입출금",
                        "상품명",
                        "상품설명",
                        "DOMESTIC"
                )
        );
        given(bankingClient.getDemandDepositProducts()).willReturn(expected);

        // When
        List<DemandDepositProductResult> result = demandDepositService.getProducts();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(bankingClient).getDemandDepositProducts();
    }

    @Test
    @DisplayName("계좌 생성 시 현재 사용자 ID를 외부 클라이언트에 전달한다.")
    void createAccountUsesCurrentUserId() {
        // Given
        DemandDepositAccountCreateResult expected = new DemandDepositAccountCreateResult(
                "001",
                "0011234567890123",
                "KRW",
                "원화"
        );
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.createDemandDepositAccount(1L, "001-1-abc")).willReturn(expected);

        // When
        DemandDepositAccountCreateResult result = demandDepositService.createAccount("001-1-abc");

        // Then
        assertThat(result).isEqualTo(expected);
        verify(bankingClient).createDemandDepositAccount(1L, "001-1-abc");
    }

    @Test
    @DisplayName("계좌 목록 조회 시 현재 사용자 ID를 외부 클라이언트에 전달한다.")
    void getAccountsUsesCurrentUserId() {
        // Given
        List<DemandDepositAccountResult> expected = List.of(
                new DemandDepositAccountResult(
                        "001",
                        "한국은행",
                        "USER",
                        "0011234567890123",
                        "한국은행 수시입출금 상품명",
                        "1",
                        "수시입출금",
                        "20240401",
                        "20290401",
                        "100000000",
                        "20000000",
                        "0",
                        "",
                        "KRW"
                )
        );
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.getDemandDepositAccounts(1L)).willReturn(expected);

        // When
        List<DemandDepositAccountResult> result = demandDepositService.getAccounts();

        // Then
        assertThat(result).isEqualTo(expected);
        verify(bankingClient).getDemandDepositAccounts(1L);
    }
}
