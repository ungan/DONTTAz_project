package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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

    @InjectMocks
    private AccountCreationService accountCreationService;

    @Test
    @DisplayName("계좌 생성 요청을 수시입출금 서비스에 위임하고 응답으로 변환한다.")
    void createAccountSuccess() {
        // Given
        CreateAccountRequest request = new CreateAccountRequest("001-1-abc");
        DemandDepositAccountCreateResult result = new DemandDepositAccountCreateResult(
                "001",
                "0011234567890123",
                "KRW",
                "원화"
        );
        given(demandDepositService.createAccount("001-1-abc")).willReturn(result);

        // When
        CreateAccountResponse response = accountCreationService.createAccount(request);

        // Then
        assertThat(response.bankCode()).isEqualTo("001");
        assertThat(response.accountNo()).isEqualTo("0011234567890123");
        assertThat(response.currency()).isEqualTo("KRW");
        assertThat(response.currencyName()).isEqualTo("원화");
        verify(demandDepositService).createAccount("001-1-abc");
    }
}
