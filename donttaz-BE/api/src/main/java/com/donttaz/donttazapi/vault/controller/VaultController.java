package com.donttaz.donttazapi.vault.controller;

import com.donttaz.donttazapi.vault.controller.docs.VaultSwagger;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.dto.request.CreateWishlistVaultRequest;
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
        @PostMapping("/default")
        public ResponseEntity<CreateVaultResponse> createDefaultVault() {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(vaultCommandService.createDefaultVault());
        }

        @Override
        @PostMapping("/wishlist")
        public ResponseEntity<CreateVaultResponse> createWishlistVault(
                        @Valid @RequestBody CreateWishlistVaultRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(vaultCommandService.createWishlistVault(request));
        }

        @Override
        @GetMapping
        public ResponseEntity<VaultListResponse> getMyVaults() {
                return ResponseEntity.ok(vaultQueryService.getMyVaults());
        }

        @Override
        @GetMapping("/wishlist")
        public ResponseEntity<VaultListResponse> getAllWishlistVaults() {
                return ResponseEntity.ok(vaultQueryService.getAllWishlistVaults());
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
        @PostMapping("/wishlist/{vaultId}/terminate")
        public ResponseEntity<VaultWithdrawResponse> terminateWishlist(
                        @PathVariable Long vaultId) {
                return ResponseEntity.ok(vaultCommandService.terminateWishlist(vaultId));
        }

        @Override
        @GetMapping("/{kind}/{vaultId}/history")
        public List<VaultHistoryResponse> getVaultHistory(
                        @PathVariable VaultKind kind,
                        @PathVariable Long vaultId) {
                return vaultQueryService.getVaultHistory(vaultId, kind);
        }
}
