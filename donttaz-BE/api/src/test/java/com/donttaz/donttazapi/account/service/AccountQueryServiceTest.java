package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private DemandDepositService demandDepositService;

    @Mock
    private AccountSyncService accountSyncService;

    @Mock
    private BankingAccountQueryService bankingAccountQueryService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountQueryService accountQueryService;

    @Test
    @DisplayName("계좌 목록 조회 시 총액을 계산하고 내부 동기화를 수행한다.")
    void getAccountsSuccess() {
        List<DemandDepositAccountResult> results = List.of(
                new DemandDepositAccountResult(
                        "020",
                        "WOORI",
                        "USER",
                        "0201238182760",
                        "Demand Deposit",
                        "1",
                        "DEPOSIT",
                        "20240320",
                        "20290320",
                        "100000000",
                        "20000000",
                        "8003477",
                        "20240323",
                        "KRW"
                )
        );
        given(demandDepositService.getAccounts()).willReturn(results);

        AccountListResponse response = accountQueryService.getAccounts();

        assertThat(response.totalBalance()).isEqualTo(8003477L);
        assertThat(response.accountList()).hasSize(1);
        assertThat(response.accountList().get(0).bankCode()).isEqualTo("020");
        assertThat(response.accountList().get(0).accountNo()).isEqualTo("020-***-182760");
        verify(demandDepositService).getAccounts();
        verify(accountSyncService).syncDemandDepositAccounts(results);
    }
}
