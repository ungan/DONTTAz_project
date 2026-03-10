package com.donttaz.donttazapi.account.controller;

import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.service.AccountCommandService;
import com.donttaz.donttazapi.account.service.AccountQueryService;
import com.donttaz.donttazapi.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "계좌", description = "수시입출금 계좌 API")
public class AccountController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    public AccountController(AccountCommandService accountCommandService, AccountQueryService accountQueryService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
    }

    @PostMapping
    @Operation(
            summary = "수시입출금 계좌 생성",
            description = "상품 조회 API에서 받은 accountTypeUniqueNo를 사용해 수시입출금 계좌를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateAccountRequest.class),
                            examples = @ExampleObject(
                                    name = "계좌 생성 요청",
                                    value = """
                                            {
                                              "accountTypeUniqueNo": "001-1-ffa4253081d540"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "계좌 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("계좌 생성 성공", accountCommandService.createAccount(request)));
    }

    @GetMapping
    @Operation(
            summary = "수시입출금 계좌 목록 조회",
            description = "finAPI에서 현재 사용자의 수시입출금 계좌 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 목록 조회 성공")
    })
    public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts() {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 목록 조회 성공", accountQueryService.getAccounts())
        );
    }

    @GetMapping("/{accountId}")
    @Operation(
            summary = "계좌 상세 조회",
            description = "레거시 상세 조회 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다."
    )
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getAccountDetail(@PathVariable Long accountId) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 상세 조회 성공", accountQueryService.getAccountDetail(accountId))
        );
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(
            summary = "거래내역 목록 조회",
            description = "레거시 거래내역 목록 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다."
    )
    public ResponseEntity<ApiResponse<TransactionListResponse>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "거래내역 목록 조회 성공",
                        accountQueryService.getTransactions(accountId, startDate, endDate, type, page, size)
                )
        );
    }

    @GetMapping("/{accountId}/transactions/{transactionId}")
    @Operation(
            summary = "거래내역 상세 조회",
            description = "레거시 거래내역 상세 API입니다. 아직 finAPI 수시입출금 흐름으로 전환되지 않았습니다."
    )
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable Long accountId,
            @PathVariable Long transactionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "거래내역 상세 조회 성공",
                        accountQueryService.getTransactionDetail(accountId, transactionId)
                )
        );
    }
}
