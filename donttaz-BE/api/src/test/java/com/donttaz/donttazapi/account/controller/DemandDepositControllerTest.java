package com.donttaz.donttazapi.account.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donttaz.donttazapi.account.service.DemandDepositService;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(controllers = DemandDepositController.class, excludeAutoConfiguration = { SecurityAutoConfiguration.class })
@Import(GlobalExceptionHandler.class)
class DemandDepositControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private DemandDepositService demandDepositService;

        @Test
        void 상품_등록_요청을_공통_응답_형식으로_반환한다() throws Exception {
                when(demandDepositService.createProduct(anyString(), anyString(), anyString()))
                                .thenReturn(new DemandDepositProductResult(
                                                "001-1-abc",
                                                "001",
                                                "한국은행",
                                                "1",
                                                "수시입출금",
                                                "한국은행 수시입출금 상품명",
                                                "한국은행 수시입출금 상품설명",
                                                "DOMESTIC"));

                mockMvc.perform(post("/api/v1/accounts/demand-deposits/products")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "bankCode": "001",
                                                  "accountName": "한국은행 수시입출금 상품명",
                                                  "accountDescription": "한국은행 수시입출금 상품설명"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status").value("SUCCESS"))
                                .andExpect(jsonPath("$.data.bankCode").value("001"))
                                .andExpect(jsonPath("$.data.accountTypeUniqueNo").value("001-1-abc"));
        }

        @Test
        void 상품_등록_요청_검증_실패시_400을_반환한다() throws Exception {
                mockMvc.perform(post("/api/v1/accounts/demand-deposits/products")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                                {
                                                  "bankCode": "",
                                                  "accountName": "상품명",
                                                  "accountDescription": "상품설명"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value("FAIL"));
        }
}
