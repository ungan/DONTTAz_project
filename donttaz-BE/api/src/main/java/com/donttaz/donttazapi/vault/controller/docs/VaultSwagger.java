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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

        @Operation(summary = "금고 생성", description = "새로운 금고를 생성합니다. 사용자는 '기본(DEFAULT)' 1개와 '위시리스트(WISHLIST)' 1개를 동시에 보유할 수 있습니다.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateVaultRequest.class))))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "금고 생성 성공")
        })
        ResponseEntity<CreateVaultResponse> createVault(@RequestBody CreateVaultRequest request);

        @Operation(summary = "내 금고 목록 조회", description = "사용자의 활성화된 금고 목록(기본 + 위시리스트)을 통합 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 목록 조회 성공")
        })
        ResponseEntity<VaultListResponse> getMyVaults();

        @Operation(summary = "금고 상세 조회", description = "특정 유형(DEFAULT/WISHLIST)의 금고 상세 정보를 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 상세 조회 성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "금고를 찾을 수 없음")
        })
        ResponseEntity<VaultSummaryResponse> getVaultDetail(@PathVariable VaultKind kind, @PathVariable Long vaultId);

        @Operation(summary = "금고 입금", description = "특정 유형의 금고로 자금을 입금합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 입금 성공")
        })
        ResponseEntity<VaultDepositResponse> deposit(@PathVariable VaultKind kind, @PathVariable Long vaultId,
                        @RequestBody VaultDepositRequest request);

        @Operation(summary = "금고 긴급 출금", description = "특정 유형의 금고에서 자금을 긴급 출금합니다. 위시리스트는 최대 3회 제한이 적용됩니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "금고 긴급 출금 성공")
        })
        ResponseEntity<VaultWithdrawResponse> withdraw(@PathVariable VaultKind kind, @PathVariable Long vaultId,
                        @RequestBody VaultWithdrawRequest request);

        @Operation(summary = "위시리스트 목표 설정/수정", description = "위시리스트 금고의 이름과 금액을 설정/수정합니다.")
        ResponseEntity<VaultSummaryResponse> setWishlistGoal(@PathVariable Long vaultId,
                        @RequestBody SetWishlistGoalRequest request);

        @Operation(summary = "위시리스트 목표 달성 (정상 해지) 처리", description = "위시리스트 금고를 정상적으로 해지 처리합니다. 무출금 시 1% 보너스가 지급됩니다.")
        ResponseEntity<VaultSummaryResponse> achieveWishlist(@PathVariable Long vaultId);

        @Operation(summary = "금고 거래 내역 조회", description = "특정 유형의 금고 거래 내역을 조회합니다.")
        ResponseEntity<com.donttaz.donttazapi.common.response.ApiResponse<List<VaultHistoryResponse>>> getVaultHistory(
                        @PathVariable VaultKind kind, @PathVariable Long vaultId);
}
