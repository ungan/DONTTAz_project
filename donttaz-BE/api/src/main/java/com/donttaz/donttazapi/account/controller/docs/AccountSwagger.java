package com.donttaz.donttazapi.account.controller.docs;

import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.AccountBalanceResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountHolderNameResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "계좌", description = "수시입출금 계좌 API")
@SecurityRequirement(name = "bearerAuth")
public interface AccountSwagger {

    @Operation(
            summary = "수시입출금 계좌 생성",
            description = "상품 조회 API에서 받은 accountTypeUniqueNo로 수시입출금 계좌를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAccountRequest.class),
                            examples = @ExampleObject(
                                    name = "계좌 생성 요청",
                                    value = "{\n  \"accountTypeUniqueNo\": \"001-1-ffa4253081d540\"\n}"
                            )
                    )
            )
    )
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

    @Operation(
            summary = "수시입출금 계좌 상세 조회",
            description = "계좌번호(accountNo)로 finAPI 수시입출금 계좌 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 상세 조회 성공")
    })
    ResponseEntity<ApiResponse<AccountDetailResponse>> getAccountDetail(@PathVariable("accountNo") String accountNo);

    @Operation(
            summary = "계좌 잔액 조회",
            description = "계좌번호(accountNo)로 finAPI 계좌 잔액 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 잔액 조회 성공")
    })
    ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(@PathVariable("accountNo") String accountNo);

    @Operation(
            summary = "예금주 조회",
            description = "계좌번호(accountNo)로 finAPI 예금주 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예금주 조회 성공")
    })
    ResponseEntity<ApiResponse<AccountHolderNameResponse>> getAccountHolderName(@PathVariable("accountNo") String accountNo);

    @Operation(
            summary = "계좌 입금",
            description = "계좌번호(accountNo)로 finAPI 입금 API를 호출합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountDepositRequest.class),
                            examples = @ExampleObject(
                                    name = "계좌 입금 요청",
                                    value = """
                                            {
                                              "transactionBalance": "100000000",
                                              "transactionSummary": "(수시입출금) : 입금"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 입금 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<AccountDepositResponse>> deposit(
            @PathVariable("accountNo") String accountNo,
            @RequestBody AccountDepositRequest request
    );

    @Operation(
            summary = "계좌 출금",
            description = "계좌번호(accountNo)로 finAPI 출금 API를 호출합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountWithdrawalRequest.class),
                            examples = @ExampleObject(
                                    name = "계좌 출금 요청",
                                    value = """
                                            {
                                              "transactionBalance": "100000",
                                              "transactionSummary": "(수시입출금) : 출금"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 출금 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<AccountWithdrawalResponse>> withdrawal(
            @PathVariable("accountNo") String accountNo,
            @RequestBody AccountWithdrawalRequest request
    );

    @Operation(summary = "거래내역 목록 조회", description = "계좌 거래내역 목록 API입니다. 현재는 로컬 저장소 기준으로 조회합니다.")
    ResponseEntity<ApiResponse<TransactionListResponse>> getTransactions(
            @PathVariable("accountId") Long accountId,
            @RequestParam(required = false, name = "startDate") LocalDate startDate,
            @RequestParam(required = false, name = "endDate") LocalDate endDate,
            @RequestParam(required = false, name = "type") String type,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "20", name = "size") int size
    );

    @Operation(summary = "거래내역 상세 조회", description = "계좌 거래내역 상세 API입니다. 현재는 로컬 저장소 기준으로 조회합니다.")
    ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable("accountId") Long accountId,
            @PathVariable("transactionId") Long transactionId
    );
}
