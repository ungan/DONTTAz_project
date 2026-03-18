package com.donttaz.donttazapi.vault.controller;

import com.donttaz.donttazapi.vault.controller.docs.VaultSwagger;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.request.SetWishlistGoalRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultListResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultWithdrawResponse;
import com.donttaz.donttazapi.vault.service.VaultCommandService;
import com.donttaz.donttazapi.vault.service.VaultQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                return ResponseEntity.ok(
                                vaultQueryService.getMyVaults());
        }

        @Override
        @GetMapping("/{vaultId}")
        public ResponseEntity<VaultSummaryResponse> getVaultDetail(
                        @PathVariable Long vaultId) {
                return ResponseEntity.ok(
                                vaultQueryService.getVaultDetail(vaultId));
        }

        @Override
        @PostMapping("/{vaultId}/deposit")
        public ResponseEntity<VaultDepositResponse> deposit(
                        @PathVariable Long vaultId,
                        @Valid @RequestBody VaultDepositRequest request) {
                return ResponseEntity.ok(
                                vaultCommandService.deposit(vaultId, request));
        }

        @Override
        @PostMapping("/{vaultId}/withdraw")
        public ResponseEntity<VaultWithdrawResponse> withdraw(
                        @PathVariable Long vaultId,
                        @Valid @RequestBody VaultWithdrawRequest request) {
                return ResponseEntity.ok(
                                vaultCommandService.withdraw(vaultId, request));
        }

        @Override
        @PatchMapping("/{vaultId}/wishlist")
        public ResponseEntity<VaultSummaryResponse> setWishlistGoal(
                        @PathVariable Long vaultId,
                        @Valid @RequestBody SetWishlistGoalRequest request) {
                return ResponseEntity.ok(
                                vaultCommandService.setWishlistGoal(vaultId, request));
        }

        @Override
        @PostMapping("/{vaultId}/achieve")
        public ResponseEntity<VaultSummaryResponse> achieveWishlist(
                        @PathVariable Long vaultId) {
                return ResponseEntity.ok(
                                vaultCommandService.achieveWishlist(vaultId));
        }

}
