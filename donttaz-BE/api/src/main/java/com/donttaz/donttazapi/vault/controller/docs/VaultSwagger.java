package com.donttaz.donttazapi.vault.controller.docs;

import com.donttaz.donttazapi.common.response.ApiResponse;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.request.SetWishlistGoalRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
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

        @Operation(summary = "금고 생성", description = "DEFAULT(기본 금고) 또는 WISHLIST(목표 금고)를 생성합니다. DEFAULT 금고는 1개만 생성 가능합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateVaultRequest.class), examples = @ExampleObject(name = "금고 생성 요청", value = "{\n  \"vaultKind\": \"WISHLIST\",\n  \"wishlistName\": \"제주도 여행\",\n  \"goalAmount\": 500000\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "금고 생성 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "DEFAULT 금고가 이미 존재함")
        })
        ResponseEntity<ApiResponse<CreateVaultResponse>> createVault(@RequestBody CreateVaultRequest request);

        @Operation(summary = "내 금고 목록 조회", description = "CLOSED 상태를 제외한 금고 목록을 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 목록 조회 성공")
        })
        ResponseEntity<ApiResponse<VaultListResponse>> getMyVaults();

        @Operation(summary = "금고 상세 조회", description = "특정 금고의 상세 정보를 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 상세 조회 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<ApiResponse<VaultSummaryResponse>> getVaultDetail(@PathVariable Long vaultId);

        @Operation(summary = "금고 입금", description = "주 계좌에서 금고로 입금합니다. 실제 SSAFY FinAPI 출금 후 금고 잔액이 증가합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaultDepositRequest.class), examples = @ExampleObject(name = "금고 입금 요청", value = "{\n  \"amount\": 100000\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 입금 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "주 계좌 잔액 부족 또는 잘못된 요청")
        })
        ResponseEntity<ApiResponse<VaultDepositResponse>> deposit(
                        @PathVariable Long vaultId,
                        @RequestBody VaultDepositRequest request);

        @Operation(summary = "금고 긴급 출금", description = "금고에서 주 계좌로 긴급 출금합니다. 금고 잔액이 감소하고 주 계좌로 입금됩니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaultWithdrawRequest.class), examples = @ExampleObject(name = "금고 긴급 출금 요청", value = "{\n  \"amount\": 50000\n}"))))

        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 긴급 출금 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "금고 잔액 부족")
        })
        ResponseEntity<ApiResponse<VaultWithdrawResponse>> withdraw(
                        @PathVariable Long vaultId,
                        @RequestBody VaultWithdrawRequest request);

        @Operation(summary = "위시리스트 목표 설정/수정", description = "WISHLIST 금고의 위시리스트 목표 이름, 금액, 날짜를 설정하거나 수정합니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetWishlistGoalRequest.class), examples = @ExampleObject(name = "위시리스트 목표 설정 요청", value = "{\n  \"wishlistName\": \"노트북 구매\",\n  \"goalAmount\": 1500000\n}"))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "위시리스트 목표 설정 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<ApiResponse<VaultSummaryResponse>> setWishlistGoal(
                        @PathVariable Long vaultId,
                        @RequestBody SetWishlistGoalRequest request);

        @Operation(summary = "WISHLIST 금고 목표 달성 처리", description = "WISHLIST 금고 잔액이 목표 금액(goal_amount) 이상일 때 달성 처리합니다. status가 CLOSED로 변경됩니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목표 달성 처리 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "목표 금액 미달"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 달성된 금고")
        })
        ResponseEntity<ApiResponse<VaultSummaryResponse>> achieveWishlist(@PathVariable Long vaultId);

}
