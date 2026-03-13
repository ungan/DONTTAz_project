package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceTest {

    @Mock
    private BankingClient bankingClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountTransferService accountTransferService;

    @Test
    @DisplayName("입금 요청을 현재 사용자 기준 외부 클라이언트에 전달한다.")
    void depositSuccess() {
        // Given
        AccountDepositRequest request = new AccountDepositRequest("100000000", "(수시입출금) : 입금");
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.deposit(any(Long.class), any(AccountDepositCommand.class)))
                .willReturn(new AccountDepositResult("59", "20240401"));

        // When
        AccountDepositResponse response = accountTransferService.deposit("0016174648358792", request);

        // Then
        ArgumentCaptor<AccountDepositCommand> commandCaptor = ArgumentCaptor.forClass(AccountDepositCommand.class);
        verify(bankingClient).deposit(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("100000000");
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("(수시입출금) : 입금");
        assertThat(response.transactionUniqueNo()).isEqualTo("59");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }

    @Test
    @DisplayName("출금 요청을 현재 사용자 기준 외부 클라이언트에 전달한다.")
    void withdrawalSuccess() {
        // Given
        AccountWithdrawalRequest request = new AccountWithdrawalRequest("100000", "(수시입출금) : 출금");
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.withdrawal(any(Long.class), any(AccountWithdrawalCommand.class)))
                .willReturn(new AccountWithdrawalResult("60", "20240401"));

        // When
        AccountWithdrawalResponse response = accountTransferService.withdrawal("0016174648358792", request);

        // Then
        ArgumentCaptor<AccountWithdrawalCommand> commandCaptor = ArgumentCaptor.forClass(AccountWithdrawalCommand.class);
        verify(bankingClient).withdrawal(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("100000");
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("(수시입출금) : 출금");
        assertThat(response.transactionUniqueNo()).isEqualTo("60");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }
}
