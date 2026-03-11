package com.donttaz.donttazapi.account.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.AccountSummaryResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.service.AccountCreationService;
import com.donttaz.donttazapi.account.service.AccountQueryService;
import com.donttaz.donttazapi.common.exception.GlobalExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountCreationService accountCreationService;

    @MockBean
    private AccountQueryService accountQueryService;

    @Test
    void 계좌_생성은_finAPI_형식의_응답을_반환한다() throws Exception {
        when(accountCreationService.createAccount(any(com.donttaz.donttazapi.account.dto.request.CreateAccountRequest.class)))
                .thenReturn(CreateAccountResponse.builder()
                        .bankCode("001")
                        .accountNo("0011234567890123")
                        .currency("KRW")
                        .currencyName("원화")
                        .build());

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "accountTypeUniqueNo": "001-1-abc"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.bankCode").value("001"))
                .andExpect(jsonPath("$.data.accountNo").value("0011234567890123"))
                .andExpect(jsonPath("$.data.currency").value("KRW"));
    }

    @Test
    void 계좌_목록_조회는_finAPI_목록을_반환한다() throws Exception {
        when(accountQueryService.getAccounts()).thenReturn(AccountListResponse.builder()
                .totalBalance(8003477L)
                .accountList(List.of(
                        AccountSummaryResponse.builder()
                                .bankCode("020")
                                .bankName("우리은행")
                                .userName("USER")
                                .accountNo("020-***-8182760")
                                .accountName("우리은행 수시입출금 상품명")
                                .accountTypeCode("1")
                                .accountTypeName("수시입출금")
                                .accountCreatedDate("20240320")
                                .accountExpiryDate("20290320")
                                .dailyTransferLimit("100000000")
                                .oneTimeTransferLimit("20000000")
                                .accountBalance("8003477")
                                .lastTransactionDate("20240323")
                                .currency("KRW")
                                .build()
                ))
                .build());

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalBalance").value(8003477))
                .andExpect(jsonPath("$.data.accountList[0].bankCode").value("020"))
                .andExpect(jsonPath("$.data.accountList[0].accountNo").value("020-***-8182760"));
    }
}
