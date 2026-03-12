package com.donttaz.donttazapi.account.controller;

import com.donttaz.donttazapi.account.controller.docs.AccountSwagger;
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
import com.donttaz.donttazapi.account.service.AccountCreationService;
import com.donttaz.donttazapi.account.service.AccountQueryService;
import com.donttaz.donttazapi.account.service.AccountTransferService;
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
public class AccountController implements AccountSwagger {

    private final AccountCreationService accountCreationService;
    private final AccountQueryService accountQueryService;
    private final AccountTransferService accountTransferService;

    public AccountController(
            AccountCreationService accountCreationService,
            AccountQueryService accountQueryService,
            AccountTransferService accountTransferService
    ) {
        this.accountCreationService = accountCreationService;
        this.accountQueryService = accountQueryService;
        this.accountTransferService = accountTransferService;
    }

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<CreateAccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("계좌 생성 성공", accountCreationService.createAccount(request)));
    }

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<AccountListResponse>> getAccounts() {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 목록 조회 성공", accountQueryService.getAccounts())
        );
    }

    @GetMapping("/{accountNo}")
    @Override
    public ResponseEntity<ApiResponse<AccountDetailResponse>> getAccountDetail(
            @PathVariable("accountNo") String accountNo
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 상세 조회 성공", accountQueryService.getAccountDetail(accountNo))
        );
    }

    @GetMapping("/{accountNo}/balance")
    @Override
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(
            @PathVariable("accountNo") String accountNo
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 잔액 조회 성공", accountQueryService.getAccountBalance(accountNo))
        );
    }

    @GetMapping("/{accountNo}/holder-name")
    @Override
    public ResponseEntity<ApiResponse<AccountHolderNameResponse>> getAccountHolderName(
            @PathVariable("accountNo") String accountNo
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("예금주 조회 성공", accountQueryService.getAccountHolderName(accountNo))
        );
    }

    @PostMapping("/{accountNo}/deposit")
    @Override
    public ResponseEntity<ApiResponse<AccountDepositResponse>> deposit(
            @PathVariable("accountNo") String accountNo,
            @Valid @RequestBody AccountDepositRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 입금 성공", accountTransferService.deposit(accountNo, request))
        );
    }

    @PostMapping("/{accountNo}/withdrawal")
    @Override
    public ResponseEntity<ApiResponse<AccountWithdrawalResponse>> withdrawal(
            @PathVariable("accountNo") String accountNo,
            @Valid @RequestBody AccountWithdrawalRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("계좌 출금 성공", accountTransferService.withdrawal(accountNo, request))
        );
    }

    @GetMapping("/{accountId}/transactions")
    @Override
    public ResponseEntity<ApiResponse<TransactionListResponse>> getTransactions(
            @PathVariable("accountId") Long accountId,
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
    @Override
    public ResponseEntity<ApiResponse<TransactionDetailResponse>> getTransactionDetail(
            @PathVariable("accountId") Long accountId,
            @PathVariable("transactionId") Long transactionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "거래내역 상세 조회 성공",
                        accountQueryService.getTransactionDetail(accountId, transactionId)
                )
        );
    }
}
