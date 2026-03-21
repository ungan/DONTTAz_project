package com.donttaz.donttazapi.account.controller.docs;

import com.donttaz.donttazapi.account.dto.request.AccountCloseRequest;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
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
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationConfirmResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationRequestResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
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

@Tag(name = "계좌", description = "계좌 API")
@SecurityRequirement(name = "bearerAuth")
public interface AccountSwagger {

        @Operation(
                        operationId = "createAccount",
                        summary = "수시입출금 계좌 생성",
                        description = "상품 조회 API에서 받은 accountTypeUniqueNo로 수시입출금 계좌를 생성합니다.",
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
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "계좌 생성 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request);

        @Operation(operationId = "getAccounts", summary = "계좌 목록 조회", description = "현재 사용자의 수시입출금 계좌 목록을 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 목록 조회 성공")
        })
        ResponseEntity<AccountListResponse> getAccounts();

        @Operation(operationId = "getAccountDetail", summary = "계좌 상세 조회", description = "계좌번호(accountNo)로 계좌 상세 정보를 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 상세 조회 성공")
        })
        ResponseEntity<AccountDetailResponse> getAccountDetail(@PathVariable("accountNo") String accountNo);

        @Operation(operationId = "getAccountBalance", summary = "계좌 잔액 조회", description = "계좌번호(accountNo)로 계좌 잔액 정보를 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 잔액 조회 성공")
        })
        ResponseEntity<AccountBalanceResponse> getAccountBalance(@PathVariable("accountNo") String accountNo);

        @Operation(operationId = "getAccountHolderName", summary = "예금주 조회", description = "계좌번호(accountNo)로 예금주 정보를 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예금주 조회 성공")
        })
        ResponseEntity<AccountHolderNameResponse> getAccountHolderName(@PathVariable("accountNo") String accountNo);

        @Operation(
                        operationId = "accountDeposit",
                        summary = "계좌 입금",
                        description = "지정한 계좌번호(accountNo)로 입금을 수행합니다. Swagger Execute에서 바로 테스트할 수 있는 계좌 입금 API입니다.",
                        security = {},
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = AccountDepositRequest.class),
                                                        examples = {
                                                                        @ExampleObject(
                                                                                        name = "계좌 입금 요청",
                                                                                        description = "기본 입금 요청 예시",
                                                                                        value = """
                                                                                                        {
                                                                                                          "transactionBalance": "1000",
                                                                                                          "transactionSummary": "입금"
                                                                                                        }
                                                                                                        """)
                                                        })))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                                        responseCode = "200",
                                        description = "계좌 입금 성공",
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = AccountDepositResponse.class),
                                                        examples = @ExampleObject(
                                                                        name = "계좌 입금 성공 응답",
                                                                        value = """
                                                                                        {
                                                                                          "transactionUniqueNo": "152308",
                                                                                          "transactionDate": "20260320"
                                                                                        }
                                                                                        """))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
        })
        ResponseEntity<AccountDepositResponse> deposit(
                        @PathVariable("accountNo") String accountNo,
                        @RequestBody AccountDepositRequest request);

        @Operation(
                        operationId = "accountWithdrawal",
                        summary = "계좌 출금",
                        description = "계좌번호(accountNo)로 출금 API를 호출합니다.",
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
                                                                                          "transactionSummary": "출금"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 출금 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<AccountWithdrawalResponse> withdrawal(
                        @PathVariable("accountNo") String accountNo,
                        @RequestBody AccountWithdrawalRequest request);

        @Operation(
                        operationId = "accountTransfer",
                        summary = "계좌 이체",
                        description = "출금 계좌에서 입금 계좌로 이체를 수행합니다.",
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = AccountTransferRequest.class),
                                                        examples = @ExampleObject(
                                                                        name = "계좌 이체 요청",
                                                                        value = """
                                                                                        {
                                                                                          "depositAccountNo": "0204667768182760",
                                                                                          "depositTransactionSummary": "입금(이체)",
                                                                                          "transactionBalance": "10000000",
                                                                                          "withdrawalAccountNo": "0016174648358792",
                                                                                          "withdrawalTransactionSummary": "출금(이체)"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 이체 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<AccountTransferResponse> transfer(@RequestBody AccountTransferRequest request);

        @Operation(
                        operationId = "requestOneWonVerification",
                        summary = "1원 송금 인증 요청",
                        description = "입력한 계좌번호로 1원 송금 인증을 요청합니다.",
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = OneWonVerificationRequest.class),
                                                        examples = @ExampleObject(
                                                                        name = "1원 송금 인증 요청",
                                                                        value = """
                                                                                        {
                                                                                          "accountNo": "0011214764051239"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "1원 송금 인증 요청 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<OneWonVerificationRequestResponse> requestOneWonVerification(
                        @RequestBody OneWonVerificationRequest request);

        @Operation(
                        operationId = "confirmOneWonVerification",
                        summary = "1원 송금 인증코드 검증",
                        description = "1원 송금 거래내역에서 확인한 4자리 인증코드를 검증합니다.",
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = OneWonVerificationConfirmRequest.class),
                                                        examples = @ExampleObject(
                                                                        name = "1원 송금 인증코드 검증 요청",
                                                                        value = """
                                                                                        {
                                                                                          "verificationId": "7",
                                                                                          "authCode": "8212"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "1원 송금 인증코드 검증 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<OneWonVerificationConfirmResponse> confirmOneWonVerification(
                        @RequestBody OneWonVerificationConfirmRequest request);

        @Operation(
                        operationId = "updateTransferLimit",
                        summary = "계좌 이체 한도 변경",
                        description = "계좌번호(accountNo)의 1회 및 1일 이체 한도를 변경합니다.",
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = AccountTransferLimitUpdateRequest.class),
                                                        examples = @ExampleObject(
                                                                        name = "계좌 이체 한도 변경 요청",
                                                                        value = """
                                                                                        {
                                                                                          "oneTimeTransferLimit": "20000000",
                                                                                          "dailyTransferLimit": "100000000"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 이체 한도 변경 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
        })
        ResponseEntity<AccountTransferLimitUpdateResponse> updateTransferLimit(
                        @PathVariable("accountNo") String accountNo,
                        @RequestBody AccountTransferLimitUpdateRequest request);

        @Operation(operationId = "setPrimaryAccount", summary = "대표 계좌 설정", description = "현재 사용자의 대표 계좌를 설정합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "대표 계좌 설정 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
        })
        ResponseEntity<Void> setPrimaryAccount(@PathVariable("accountNo") String accountNo);

        @Operation(
                        operationId = "closeAccount",
                        summary = "계좌 해지",
                        description = "은행 코드와 계좌번호로 해지 대상을 지정하고 환급 계좌 정보로 계좌 해지 API를 호출합니다.",
                        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                        required = true,
                                        content = @Content(
                                                        mediaType = "application/json",
                                                        schema = @Schema(implementation = AccountCloseRequest.class),
                                                        examples = @ExampleObject(
                                                                        name = "계좌 해지 요청",
                                                                        value = """
                                                                                        {
                                                                                          "refundBankCode": "032",
                                                                                          "refundAccountNo": "0324003842129948"
                                                                                        }
                                                                                        """))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계좌 해지 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "계좌를 찾을 수 없음")
        })
        ResponseEntity<AccountCloseResponse> closeAccount(
                        @PathVariable("bankCode") String bankCode,
                        @PathVariable("accountNo") String accountNo,
                        @RequestBody AccountCloseRequest request);

        @Operation(operationId = "getTransactions", summary = "거래내역 목록 조회", description = "계좌 거래내역 목록을 조회합니다.")
        ResponseEntity<TransactionListResponse> getTransactions(
                        @PathVariable("accountId") Long accountId,
                        @RequestParam(required = false, name = "startDate") LocalDate startDate,
                        @RequestParam(required = false, name = "endDate") LocalDate endDate,
                        @RequestParam(required = false, name = "type") String type,
                        @RequestParam(defaultValue = "0", name = "page") int page,
                        @RequestParam(defaultValue = "20", name = "size") int size);

        @Operation(operationId = "getTransactionDetail", summary = "거래내역 상세 조회", description = "계좌 거래내역 상세를 조회합니다.")
        ResponseEntity<TransactionDetailResponse> getTransactionDetail(
                        @PathVariable("accountId") Long accountId,
                        @PathVariable("transactionId") String transactionId);
}
