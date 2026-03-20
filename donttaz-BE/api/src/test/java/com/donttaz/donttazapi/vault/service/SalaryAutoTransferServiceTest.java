package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryAutoTransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankingClient bankingClient;

    @Mock
    private AiPlanService aiPlanService;

    @Mock
    private VaultCommandService vaultCommandService;

    @InjectMocks
    private SalaryAutoTransferService salaryAutoTransferService;

    @Test
    @DisplayName("당일 급여 입금이 확인되면 금고로 자동 이체를 실행한다")
    void executeSalaryAutoTransfer_Success() {
        // given
        int todayDay = LocalDate.now().getDayOfMonth();
        User user = User.builder()
                .userId(1L)
                .name("테스트유저")
                .salaryDay(todayDay)
                .salaryName("급여")
                .build();

        Account primaryAccount = Account.builder()
                .userId(1L)
                .accountNumber("123456789")
                .isPrimary(true)
                .status(AccountStatus.ACTIVE)
                .build();

        TransactionSummaryResult salaryTransaction = new TransactionSummaryResult(
                "tx-123", LocalDateTime.now(), "DEPOSIT", 3000000, 5000000, "급여", "GENERAL");

        TransactionQueryResult txResult = new TransactionQueryResult(
                1, 1, true, Collections.singletonList(salaryTransaction));

        given(userRepository.findAllBySalaryDay(todayDay)).willReturn(List.of(user));
        given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(anyLong(), any()))
                .willReturn(Optional.of(primaryAccount));
        given(bankingClient.getTransactions(anyLong(), anyString(), any(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(txResult);
        given(aiPlanService.getPlannedDepositAmount(anyLong(), anyString())).willReturn(500000L);

        // when
        salaryAutoTransferService.executeSalaryAutoTransfer();

        // then
        verify(vaultCommandService, times(1)).depositInternal(eq(1L), eq("123456789"), eq(500000L));
    }

    @Test
    @DisplayName("급여 입금이 확인되지 않으면 이체를 실행하지 않는다")
    void executeSalaryAutoTransfer_NoDeposit() {
        // given
        int todayDay = LocalDate.now().getDayOfMonth();
        User user = User.builder()
                .userId(1L)
                .salaryDay(todayDay)
                .salaryName("급여")
                .build();

        Account primaryAccount = Account.builder()
                .userId(1L)
                .isPrimary(true)
                .status(AccountStatus.ACTIVE)
                .build();

        TransactionQueryResult txResult = new TransactionQueryResult(0, 0, true, Collections.emptyList());

        given(userRepository.findAllBySalaryDay(todayDay)).willReturn(List.of(user));
        given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(anyLong(), any()))
                .willReturn(Optional.of(primaryAccount));
        given(bankingClient.getTransactions(anyLong(), anyString(), any(), any(), anyString(), anyInt(), anyInt()))
                .willReturn(txResult);

        // when
        salaryAutoTransferService.executeSalaryAutoTransfer();

        // then
        verify(vaultCommandService, never()).depositInternal(anyLong(), anyString(), anyLong());
    }
}
