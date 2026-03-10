package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemandDepositServiceTest {

    @Mock
    private BankingClient bankingClient;

    @InjectMocks
    private DemandDepositService demandDepositService;

    @Test
    void 상품_등록을_외부_클라이언트에_위임한다() {
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
        when(bankingClient.createDemandDepositProduct("001", "상품명", "상품설명")).thenReturn(expected);

        DemandDepositProductResult result = demandDepositService.createProduct("001", "상품명", "상품설명");

        assertThat(result).isEqualTo(expected);
        verify(bankingClient).createDemandDepositProduct("001", "상품명", "상품설명");
    }

    @Test
    void 계좌_생성시_고정_사용자_ID를_전달한다() {
        DemandDepositAccountCreateResult expected = new DemandDepositAccountCreateResult(
                "001",
                "0011234567890123",
                "KRW",
                "원화"
        );
        when(bankingClient.createDemandDepositAccount(1L, "001-1-abc")).thenReturn(expected);

        DemandDepositAccountCreateResult result = demandDepositService.createAccount("001-1-abc");

        assertThat(result).isEqualTo(expected);
        verify(bankingClient).createDemandDepositAccount(1L, "001-1-abc");
    }

    @Test
    void 계좌_목록_조회시_고정_사용자_ID를_전달한다() {
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
        when(bankingClient.getDemandDepositAccounts(1L)).thenReturn(expected);

        List<DemandDepositAccountResult> result = demandDepositService.getAccounts();

        assertThat(result).isEqualTo(expected);
        verify(bankingClient).getDemandDepositAccounts(1L);
    }
}
