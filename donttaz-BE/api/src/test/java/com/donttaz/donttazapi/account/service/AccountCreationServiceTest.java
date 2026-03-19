package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountCreationServiceTest {

    @Mock
    private DemandDepositService demandDepositService;

    @Mock
    private AccountSyncService accountSyncService;

    @InjectMocks
    private AccountCreationService accountCreationService;

    @Test
    @DisplayName("계좌 생성 요청을 외부 서비스에 위임하고 내부 동기화를 시도한다.")
    void createAccountSuccess() {
        CreateAccountRequest request = new CreateAccountRequest("001-1-abc");
        DemandDepositAccountCreateResult result = new DemandDepositAccountCreateResult(
                "001",
                "0011234567890123",
                "KRW",
                "Korean Won");
        given(demandDepositService.createAccount("001-1-abc")).willReturn(result);

        CreateAccountResponse response = accountCreationService.createAccount(request);

        assertThat(response.bankCode()).isEqualTo("001");
        assertThat(response.accountNo()).isEqualTo("0011234567890123");
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.currencyName()).isEqualTo("Korean Won");
        verify(demandDepositService).createAccount("001-1-abc");

        verify(accountSyncService).syncCreatedAccount("001-1-abc", "0011234567890123");
    }

    @Test
    @DisplayName("내부 동기화에 실패해도 외부 계좌 생성 응답은 반환한다.")
    void createAccountReturnsResponseEvenIfSyncFails() {
        CreateAccountRequest request = new CreateAccountRequest("001-1-abc");
        DemandDepositAccountCreateResult result = new DemandDepositAccountCreateResult(
                "001",
                "0011234567890123",
                "KRW",
                "Korean Won");
        given(demandDepositService.createAccount("001-1-abc")).willReturn(result);

        doThrow(new IllegalStateException("sync failed"))
                .when(accountSyncService)
                .syncCreatedAccount("001-1-abc", "0011234567890123");

        CreateAccountResponse response = accountCreationService.createAccount(request);

        assertThat(response.bankCode()).isEqualTo("001");
        assertThat(response.accountNo()).isEqualTo("0011234567890123");
        verify(accountSyncService).syncCreatedAccount("001-1-abc", "0011234567890123");
    }
}
