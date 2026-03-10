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
public class AccountController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    public AccountController(AccountCommandService accountCommandService, AccountQueryService accountQueryService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("계좌가 성공적으로 등록되었습니다.", accountCommandService.createAccount(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts() {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 목록 조회 성공", accountQueryService.getAccounts())
        );
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getAccountDetail(@PathVariable Long accountId) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 상세 정보 조회 성공", accountQueryService.getAccountDetail(accountId))
        );
    }

    @GetMapping("/{accountId}/transactions")
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
                        "거래 내역 조회 성공",
                        accountQueryService.getTransactions(accountId, startDate, endDate, type, page, size)
                )
        );
    }

    @GetMapping("/{accountId}/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable Long accountId,
            @PathVariable Long transactionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "거래 내역 상세 조회 성공",
                        accountQueryService.getTransactionDetail(accountId, transactionId)
                )
        );
    }
}
