package com.donttaz.donttazapi.vault.controller.docs;

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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "금고", description = "가상 금고 관리 API")
@SecurityRequirement(name = "bearerAuth")
public interface VaultSwagger {

        @Operation(summary = "금고 생성", description = "새로운 금고(기본적으로 LOCKED 상태)를 생성합니다. 기본 금고(DEFAULT)는 사용자당 1개만 가능하며, 목표 금고(WISHLIST)는 목표 정보를 필수로 입력해야 합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateVaultRequest.class), examples = @ExampleObject(name = "목표 금고 생성 요청", value = "{\n  \"vaultKind\": \"WISHLIST\",\n  \"wishName\": \"제주도 여행\",\n  \"goalAmount\": 500000,\n  \"targetDate\": \"2026-12-31\"\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "금고 생성 성공")
        })
        ResponseEntity<CreateVaultResponse> createVault(@RequestBody CreateVaultRequest request);

        @Operation(summary = "내 금고 목록 조회", description = "사용자의 활성화된(LOCKED) 금고 목록을 조회합니다. 각 금고의 현재 잔액과 적용 이자율 정보가 포함됩니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 목록 조회 성공")
        })
        ResponseEntity<VaultListResponse> getMyVaults();

        @Operation(summary = "금고 상세 조회", description = "특정 금고의 상세 정보를 조회합니다. 목표 금액, 달성 날짜, 현재 적용 중인 기본 이자율 및 패널티 이자율을 확인할 수 있습니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 상세 조회 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultSummaryResponse> getVaultDetail(@PathVariable Long vaultId);

        @Operation(summary = "금고 입금", description = "금고로 자금을 입금합니다. 입금 시 'DEPOSIT' 타입의 거래 내역이 생성됩니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaultDepositRequest.class), examples = @ExampleObject(name = "금고 입금 요청", value = "{\n  \"accountNo\": \"10000000001\",\n  \"amount\": 100000\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 입금 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultDepositResponse> deposit(
                        @PathVariable Long vaultId,
                        @RequestBody VaultDepositRequest request);

        @Operation(summary = "금고 긴급 출금", description = "금고에서 자금을 긴급(중도) 출금합니다. 출금 시점에 계산된 이자가 지급되지만, 패널티 이자율이 증가합니다. 'WITHDRAW_BROKEN' 타입의 거래 내역이 생성됩니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaultWithdrawRequest.class), examples = @ExampleObject(name = "금고 긴급 출금 요청", value = "{\n  \"accountNo\": \"10000000001\",\n  \"amount\": 50000\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 긴급 출금 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "금고 잔액 부족 또는 잘못된 상태")
        })
        ResponseEntity<VaultWithdrawResponse> withdraw(
                        @PathVariable Long vaultId,
                        @RequestBody VaultWithdrawRequest request);

        @Operation(summary = "위시리스트 목표 설정/수정", description = "금고의 위시리스트 목표 이름, 금액, 날짜를 설정하거나 수정합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetWishlistGoalRequest.class), examples = @ExampleObject(name = "위시리스트 목표 설정 요청", value = "{\n  \"wishName\": \"노트북 구매\",\n  \"goalAmount\": 1500000,\n  \"targetDate\": \"2026-12-31\"\n}"))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "위시리스트 목표 설정 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultSummaryResponse> setWishlistGoal(
                        @PathVariable Long vaultId,
                        @RequestBody SetWishlistGoalRequest request);

        @Operation(summary = "금고 목표 달성 (정상 해지) 처리", description = "금고를 정상적으로 해지(MATURED) 처리합니다. 누적된 이자와 원금이 합산되어 지급되며, 'WITHDRAW_MATURED' 타입의 거래 내역이 생성됩니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목표 달성 처리 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultSummaryResponse> achieveWishlist(@PathVariable Long vaultId);

        @Operation(summary = "금고 거래 내역 조회", description = "특정 금고의 모든 입/출금 및 해지 내역을 최신순으로 조회합니다. 거래 타입은 DEPOSIT(입금), WITHDRAW_BROKEN(긴급 출금), WITHDRAW_MATURED(정상 해지) 중 하나입니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "거래 내역 조회 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<com.donttaz.donttazapi.common.response.ApiResponse<java.util.List<VaultHistoryResponse>>> getVaultHistory(
                        @PathVariable Long vaultId);
}
