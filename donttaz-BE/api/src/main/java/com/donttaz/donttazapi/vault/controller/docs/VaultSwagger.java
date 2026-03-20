package com.donttaz.donttazapi.vault.controller.docs;

import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.request.SetWishlistGoalRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultHistoryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultListResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultWithdrawResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "금고", description = "가상 금고 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface VaultSwagger {

        @Operation(summary = "금고 생성", description = "새로운 금고를 생성합니다. 사용자는 '기본(DEFAULT)' 1개와 '위시리스트(WISHLIST)' 1개를 동시에 보유할 수 있습니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "금고 생성 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "기본 금고 생성 예시", value = "{\"vaultId\": 1, \"vaultKind\": \"DEFAULT\", \"lockedAmount\": 0, \"status\": \"LOCKED\"}"),
                                        @ExampleObject(name = "위시리스트 금고 생성 예시", value = "{\"vaultId\": 10, \"vaultKind\": \"WISHLIST\", \"lockedAmount\": 0, \"status\": \"LOCKED\"}")
                        }))
        })
        ResponseEntity<CreateVaultResponse> createVault(@RequestBody CreateVaultRequest request);

        @Operation(summary = "내 금고 목록 조회", description = "사용자의 활성화된 금고 목록(기본 + 위시리스트)을 통합 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "금고 목록 조회 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "통합 목록 예시", value = "{\"vaults\": [" +
                                                        "{\"vaultId\": 1, \"vaultKind\": \"DEFAULT\", \"lockedAmount\": 1000000, \"status\": \"LOCKED\", \"wishName\": \"기본 금고\", \"goalAmount\": 0, \"baseInterestRate\": 4.0, \"penaltyInterestRate\": 0.0, \"withdrawalCount\": 0},"
                                                        +
                                                        "{\"vaultId\": 10, \"vaultKind\": \"WISHLIST\", \"lockedAmount\": 500000, \"status\": \"LOCKED\", \"wishName\": \"애플워치 구매\", \"goalAmount\": 900000, \"baseInterestRate\": 6.0, \"penaltyInterestRate\": 0.3, \"withdrawalCount\": 1}"
                                                        +
                                                        "]}")
                        }))
        })
        ResponseEntity<VaultListResponse> getMyVaults();

        @Operation(summary = "금고 상세 조회", description = "특정 유형(DEFAULT/WISHLIST)의 금고 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "금고 상세 조회 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "기본 금고 상세", value = "{\"vaultId\": 1, \"vaultKind\": \"DEFAULT\", \"lockedAmount\": 1000000, \"status\": \"LOCKED\", \"wishName\": \"기본 금고\", \"goalAmount\": 0, \"baseInterestRate\": 4.0, \"penaltyInterestRate\": 0.0, \"withdrawalCount\": 0}"),
                                        @ExampleObject(name = "위시리스트 금고 상세", value = "{\"vaultId\": 10, \"vaultKind\": \"WISHLIST\", \"lockedAmount\": 500000, \"status\": \"LOCKED\", \"wishName\": \"애플워치 구매\", \"goalAmount\": 900000, \"baseInterestRate\": 6.0, \"penaltyInterestRate\": 0.3, \"withdrawalCount\": 1}")
                        })),
                        @ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultSummaryResponse> getVaultDetail(
                        @Parameter(description = "금고 종류", example = "WISHLIST") @PathVariable VaultKind kind,
                        @Parameter(description = "금고 ID", example = "10") @PathVariable Long vaultId);

        @Operation(summary = "금고 입금", description = "특정 유형의 금고로 자금을 입금합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "금고 입금 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "기본 금고 입금 결과", value = "{\"vaultId\": 1, \"depositedAmount\": 100000, \"lockedAmount\": 1100000}"),
                                        @ExampleObject(name = "위시리스트 금고 입금 결과", value = "{\"vaultId\": 10, \"depositedAmount\": 50000, \"lockedAmount\": 550000}")
                        }))
        })
        ResponseEntity<VaultDepositResponse> deposit(
                        @Parameter(description = "금고 종류", example = "DEFAULT") @PathVariable VaultKind kind,
                        @Parameter(description = "금고 ID", example = "1") @PathVariable Long vaultId,
                        @RequestBody VaultDepositRequest request);

        @Operation(summary = "금고 긴급 출금", description = "특정 유형의 금고에서 자금을 긴급 출금합니다. 위시리스트는 최대 3회 제한이 적용됩니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "금고 긴급 출금 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "기본 금고 긴급 출금 결과", value = "{\"vaultId\": 1, \"withdrawnAmount\": 50000, \"interestPaid\": 150, \"totalRefundAmount\": 50150, \"withdrawalCount\": 1}"),
                                        @ExampleObject(name = "위시리스트 금고 긴급 출금 결과", value = "{\"vaultId\": 10, \"withdrawnAmount\": 30000, \"interestPaid\": 45, \"totalRefundAmount\": 30045, \"withdrawalCount\": 2}")
                        }))
        })
        ResponseEntity<VaultWithdrawResponse> withdraw(
                        @Parameter(description = "금고 종류", example = "WISHLIST") @PathVariable VaultKind kind,
                        @Parameter(description = "금고 ID", example = "10") @PathVariable Long vaultId,
                        @RequestBody VaultWithdrawRequest request);

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목표 수정 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "위시리스트 목표 수정 결과", value = "{\"vaultId\": 10, \"vaultKind\": \"WISHLIST\", \"lockedAmount\": 500000, \"status\": \"LOCKED\", \"wishName\": \"갤럭시북 구매\", \"goalAmount\": 2500000, \"baseInterestRate\": 6.0, \"penaltyInterestRate\": 0.3, \"withdrawalCount\": 1}")
                        }))
        })
        ResponseEntity<VaultSummaryResponse> setWishlistGoal(
                        @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId,
                        @RequestBody SetWishlistGoalRequest request);

        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목표 달성 처리 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "위시리스트 목표 달성 결과", value = "{\"vaultId\": 10, \"vaultKind\": \"WISHLIST\", \"lockedAmount\": 909000, \"status\": \"MATURED\", \"wishName\": \"애플워치 구매\", \"goalAmount\": 900000, \"baseInterestRate\": 6.0, \"penaltyInterestRate\": 0.0, \"withdrawalCount\": 0}")
                        }))
        })
        ResponseEntity<VaultSummaryResponse> achieveWishlist(
                        @Parameter(description = "위시리스트 금고 ID", example = "10") @PathVariable Long vaultId);

        @Operation(summary = "금고 거래 내역 조회", description = "특정 유형의 금고 거래 내역을 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "금고 거래 내역 조회 성공", content = @Content(mediaType = "application/json", examples = {
                                        @ExampleObject(name = "기본 금고 거래 내역 예시", value = "{\"status\": \"SUCCESS\", \"message\": \"거래 내역 조회 성공\", \"data\": ["
                                                        + "{\"id\": 1, \"vaultId\": 1, \"transactionType\": \"DEPOSIT\", \"amount\": 100000, \"interestPaid\": 0, \"createdAt\": \"2024-03-21T10:00:00\"},"
                                                        + "{\"id\": 2, \"vaultId\": 1, \"transactionType\": \"WITHDRAW_BROKEN\", \"amount\": 50000, \"interestPaid\": 150, \"createdAt\": \"2024-03-21T15:30:00\"}"
                                                        + "]}"),
                                        @ExampleObject(name = "위시리스트 금고 거래 내역 예시", value = "{\"status\": \"SUCCESS\", \"message\": \"거래 내역 조회 성공\", \"data\": ["
                                                        + "{\"id\": 10, \"vaultId\": 10, \"transactionType\": \"DEPOSIT\", \"amount\": 500000, \"interestPaid\": 0, \"createdAt\": \"2024-03-21T11:00:00\"},"
                                                        + "{\"id\": 11, \"vaultId\": 10, \"transactionType\": \"WITHDRAW_BROKEN\", \"amount\": 100000, \"interestPaid\": 450, \"createdAt\": \"2024-03-21T16:00:00\"}"
                                                        + "]}")
                        }))
        })
        ResponseEntity<com.donttaz.donttazapi.common.response.ApiResponse<List<VaultHistoryResponse>>> getVaultHistory(
                        @Parameter(description = "금고 종류", example = "DEFAULT") @PathVariable VaultKind kind,
                        @Parameter(description = "금고 ID", example = "1") @PathVariable Long vaultId);
}
