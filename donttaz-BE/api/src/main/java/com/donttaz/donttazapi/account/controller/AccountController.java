package com.donttaz.donttazapi.account.controller;

import com.donttaz.donttazapi.account.controller.docs.AccountSwagger;
import com.donttaz.donttazapi.account.dto.request.AccountCloseRequest;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationConfirmRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationRequest;
import com.donttaz.donttazapi.account.dto.response.AccountBalanceResponse;
import com.donttaz.donttazapi.account.dto.response.AccountCloseResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountDetailResponse;
import com.donttaz.donttazapi.account.dto.response.AccountHolderNameResponse;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.AccountSummaryResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationConfirmResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationRequestResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.service.AccountCreationService;
import com.donttaz.donttazapi.account.service.AccountManagementService;
import com.donttaz.donttazapi.account.service.AccountQueryService;
import com.donttaz.donttazapi.account.service.AccountTransferService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
        private final AccountManagementService accountManagementService;
        private final AccountQueryService accountQueryService;
        private final AccountTransferService accountTransferService;

        public AccountController(
                        AccountCreationService accountCreationService,
                        AccountManagementService accountManagementService,
                        AccountQueryService accountQueryService,
                        AccountTransferService accountTransferService) {
                this.accountCreationService = accountCreationService;
                this.accountManagementService = accountManagementService;
                this.accountQueryService = accountQueryService;
                this.accountTransferService = accountTransferService;
        }

        @PostMapping
        @Override
        public ResponseEntity<CreateAccountResponse> createAccount(
                        @Valid @RequestBody CreateAccountRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(accountCreationService.createAccount(request));
        }

        @GetMapping
        @Override
        public ResponseEntity<AccountListResponse> getAccounts() {
                return ResponseEntity.ok(
                                accountQueryService.getAccounts());
        }

        @GetMapping("/primary")
        @Override
        public ResponseEntity<AccountSummaryResponse> getPrimaryAccount() {
                return ResponseEntity.ok(
                                accountQueryService.getPrimaryAccount());
        }

        @GetMapping("/{accountNo}")
        @Override
        public ResponseEntity<AccountDetailResponse> getAccountDetail(
                        @PathVariable("accountNo") String accountNo) {
                return ResponseEntity.ok(
                                accountQueryService.getAccountDetail(accountNo));
        }

        @GetMapping("/{accountNo}/balance")
        @Override
        public ResponseEntity<AccountBalanceResponse> getAccountBalance(
                        @PathVariable("accountNo") String accountNo) {
                return ResponseEntity.ok(
                                accountQueryService.getAccountBalance(accountNo));
        }

        @GetMapping("/{accountNo}/holder-name")
        @Override
        public ResponseEntity<AccountHolderNameResponse> getAccountHolderName(
                        @PathVariable("accountNo") String accountNo) {
                return ResponseEntity.ok(
                                accountQueryService.getAccountHolderName(accountNo));
        }

        @PostMapping("/{accountNo}/deposit")
        @Override
        public ResponseEntity<AccountDepositResponse> deposit(
                        @PathVariable("accountNo") String accountNo,
                        @Valid @RequestBody AccountDepositRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.deposit(accountNo, request));
        }

        @PostMapping("/{accountNo}/withdrawal")
        @Override
        public ResponseEntity<AccountWithdrawalResponse> withdrawal(
                        @PathVariable("accountNo") String accountNo,
                        @Valid @RequestBody AccountWithdrawalRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.withdrawal(accountNo, request));
        }

        @PostMapping("/transfer")
        @Override
        public ResponseEntity<AccountTransferResponse> transfer(
                        @Valid @RequestBody AccountTransferRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.transfer(request));
        }

        @PostMapping("/verification/one-won")
        @Override
        public ResponseEntity<OneWonVerificationRequestResponse> requestOneWonVerification(
                        @Valid @RequestBody OneWonVerificationRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.requestOneWonVerification(request));
        }

        @PostMapping("/verification/one-won/confirm")
        @Override
        public ResponseEntity<OneWonVerificationConfirmResponse> confirmOneWonVerification(
                        @Valid @RequestBody OneWonVerificationConfirmRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.confirmOneWonVerification(request));
        }

        @PatchMapping("/{accountNo}/transfer-limit")
        @Override
        public ResponseEntity<AccountTransferLimitUpdateResponse> updateTransferLimit(
                        @PathVariable("accountNo") String accountNo,
                        @Valid @RequestBody AccountTransferLimitUpdateRequest request) {
                return ResponseEntity.ok(
                                accountTransferService.updateTransferLimit(accountNo, request));
        }

        @PatchMapping("/{accountNo}/primary")
        @Override
        public ResponseEntity<Void> setPrimaryAccount(
                        @PathVariable("accountNo") String accountNo) {
                accountManagementService.setPrimaryAccount(accountNo);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/{bankCode}/{accountNo}/close")
        @Override
        public ResponseEntity<AccountCloseResponse> closeAccount(
                        @PathVariable("bankCode") String bankCode,
                        @PathVariable("accountNo") String accountNo,
                        @Valid @RequestBody AccountCloseRequest request) {
                return ResponseEntity.ok(
                                accountManagementService.closeAccount(bankCode, accountNo, request));
        }

        @GetMapping("/{accountId}/transactions")
        @Override
        public ResponseEntity<TransactionListResponse> getTransactions(
                        @PathVariable("accountId") Long accountId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) String type,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                return ResponseEntity.ok(
                                accountQueryService.getTransactions(accountId, startDate, endDate, type, page, size));
        }

        @GetMapping("/{accountId}/transactions/{transactionId}")
        @Override
        public ResponseEntity<TransactionDetailResponse> getTransactionDetail(
                        @PathVariable("accountId") Long accountId,
                        @PathVariable("transactionId") String transactionId) {
                return ResponseEntity.ok(
                                accountQueryService.getTransactionDetail(accountId, transactionId));
        }
}
