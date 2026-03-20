package com.donttaz.donttazapi.vault.controller;

import com.donttaz.donttazapi.vault.controller.docs.VaultSwagger;
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
import com.donttaz.donttazapi.vault.service.VaultCommandService;
import com.donttaz.donttazapi.vault.service.VaultQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vaults")
public class VaultController implements VaultSwagger {

        private final VaultCommandService vaultCommandService;
        private final VaultQueryService vaultQueryService;

        public VaultController(VaultCommandService vaultCommandService, VaultQueryService vaultQueryService) {
                this.vaultCommandService = vaultCommandService;
                this.vaultQueryService = vaultQueryService;
        }

        @Override
        @PostMapping
        public ResponseEntity<CreateVaultResponse> createVault(
                        @Valid @RequestBody CreateVaultRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(vaultCommandService.createVault(request));
        }

        @Override
        @GetMapping
        public ResponseEntity<VaultListResponse> getMyVaults() {
                return ResponseEntity.ok(vaultQueryService.getMyVaults());
        }

        @Override
        @GetMapping("/{kind}/{vaultId}")
        public ResponseEntity<VaultSummaryResponse> getVaultDetail(
                        @PathVariable VaultKind kind,
                        @PathVariable Long vaultId) {
                return ResponseEntity.ok(vaultQueryService.getVaultDetail(vaultId, kind));
        }

        @Override
        @PostMapping("/{kind}/{vaultId}/deposit")
        public ResponseEntity<VaultDepositResponse> deposit(
                        @PathVariable VaultKind kind,
                        @PathVariable Long vaultId,
                        @Valid @RequestBody VaultDepositRequest request) {
                return ResponseEntity.ok(vaultCommandService.deposit(vaultId, kind, request));
        }

        @Override
        @PostMapping("/{kind}/{vaultId}/withdraw")
        public ResponseEntity<VaultWithdrawResponse> withdraw(
                        @PathVariable VaultKind kind,
                        @PathVariable Long vaultId,
                        @Valid @RequestBody VaultWithdrawRequest request) {
                return ResponseEntity.ok(vaultCommandService.withdraw(vaultId, kind, request));
        }

        @Override
        @PatchMapping("/wishlist/{vaultId}")
        public ResponseEntity<VaultSummaryResponse> setWishlistGoal(
                        @PathVariable Long vaultId,
                        @Valid @RequestBody SetWishlistGoalRequest request) {
                vaultCommandService.setWishlistGoal(vaultId, request);
                return ResponseEntity.ok(vaultQueryService.getVaultDetail(vaultId, VaultKind.WISHLIST));
        }

        @Override
        @PostMapping("/wishlist/{vaultId}/achieve")
        public ResponseEntity<VaultSummaryResponse> achieveWishlist(
                        @PathVariable Long vaultId) {
                vaultCommandService.achieveWishlist(vaultId);
                return ResponseEntity.ok(vaultQueryService.getVaultDetail(vaultId, VaultKind.WISHLIST));
        }

        @Override
        @GetMapping("/{kind}/{vaultId}/history")
        public ResponseEntity<com.donttaz.donttazapi.common.response.ApiResponse<List<VaultHistoryResponse>>> getVaultHistory(
                        @PathVariable VaultKind kind,
                        @PathVariable Long vaultId) {
                return ResponseEntity.ok(
                                com.donttaz.donttazapi.common.response.ApiResponse.success("거래 내역 조회 성공",
                                                vaultQueryService.getVaultHistory(vaultId, kind)));
        }
}
