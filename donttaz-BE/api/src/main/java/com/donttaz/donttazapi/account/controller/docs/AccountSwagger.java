package com.donttaz.donttazapi.account.controller.docs;

import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "계좌", description = "수시입출금 계좌 API")
public interface AccountSwagger {

        @Operation(summary = "수시입출금 계좌 생성", description = "상품 조회 API에서 받은 accountTypeUniqueNo를 사용해 수시입출금 계좌를 생성합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateAccountRequest.class), examples = @ExampleObject(name = "계좌 생성 요청", value = "{\n  \"accountTypeUniqueNo\": \"001-1-ffa4253081d540\"\n}"))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "계좌 생성 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(@RequestBody CreateAccountRequest request);

        @Operation(summary = "수시입출금 계좌 목록 조회", description = "finAPI에서 현재 사용자의 수시입출금 계좌 목록을 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 목록 조회 성공")
        })
        ResponseEntity<ApiResponse<AccountListResponse>> getAccounts();

        @Operation(summary = "계좌 상세 조회", description = "레거시 상세 조회 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다.")
        ResponseEntity<ApiResponse<AccountDetailResponse>> getAccountDetail(
                        @PathVariable("accountId") Long accountId);

        @Operation(summary = "거래내역 목록 조회", description = "레거시 거래내역 목록 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다.")
        ResponseEntity<ApiResponse<TransactionListResponse>> getTransactions(
                        @PathVariable("accountId") Long accountId,
                        @RequestParam(required = false, name = "startDate") LocalDate startDate,
                        @RequestParam(required = false, name = "endDate") LocalDate endDate,
                        @RequestParam(required = false, name = "type") String type,
                        @RequestParam(defaultValue = "0", name = "page") int page,
                        @RequestParam(defaultValue = "20", name = "size") int size);

        @Operation(summary = "거래내역 상세 조회", description = "레거시 거래내역 상세 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다.")
        ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
                        @PathVariable("accountId") Long accountId,
                        @PathVariable("transactionId") Long transactionId);
}
