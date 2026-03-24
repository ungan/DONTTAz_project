package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryAutoTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AiPlanService aiPlanService;

    @Mock
    private VaultCommandService vaultCommandService;

    @InjectMocks
    private SalaryAutoTransferService salaryAutoTransferService;

    @Test
    @DisplayName("당일 급여 입금이 확인되면 금고로 자동 이체를 실행한다")
    void processUserSalaryTransfer_Success() {
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

        given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(anyLong(), any()))
                .willReturn(Optional.of(primaryAccount));
        given(aiPlanService.getPlannedDepositAmount(anyLong(), anyString())).willReturn(500000L);

        // when
        salaryAutoTransferService.processUserSalaryTransfer(user);

        // then
        verify(vaultCommandService, times(1)).depositInternal(eq(1L), eq("123456789"), eq(500000L));
    }
}
