package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.DefaultVault;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.response.VaultHistoryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultListResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.vault.repository.DefaultVaultRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VaultQueryService {

        private final DefaultVaultRepository defaultVaultRepository;
        private final WishlistVaultRepository wishlistVaultRepository;
        private final VaultHistoryRepository vaultHistoryRepository;
        private final VaultCommandService vaultCommandService;
        private final CurrentUserProvider currentUserProvider;

        public VaultQueryService(
                        DefaultVaultRepository defaultVaultRepository,
                        WishlistVaultRepository wishlistVaultRepository,
                        VaultHistoryRepository vaultHistoryRepository,
                        @Lazy VaultCommandService vaultCommandService,
                        CurrentUserProvider currentUserProvider) {
                this.defaultVaultRepository = defaultVaultRepository;
                this.wishlistVaultRepository = wishlistVaultRepository;
                this.vaultHistoryRepository = vaultHistoryRepository;
                this.vaultCommandService = vaultCommandService;
                this.currentUserProvider = currentUserProvider;
        }

        @Transactional
        public VaultListResponse getMyVaults() {
                Long userId = currentUserProvider.getCurrentUserId();

                // 기본 금고 생성 보장
                vaultCommandService.createDefaultVault(userId);

                List<VaultSummaryResponse> summaries = new ArrayList<>();

                // 1. 기본 금고 추가
                defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                .ifPresent(v -> summaries.add(toSummaryResponse(v)));

                // 2. 위시리스트 금고 추가
                wishlistVaultRepository.findAllByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                .forEach(v -> summaries.add(toSummaryResponse(v)));

                return VaultListResponse.builder()
                                .vaults(summaries)
                                .build();
        }

        public VaultListResponse getAllWishlistVaults() {
                Long userId = currentUserProvider.getCurrentUserId();

                List<VaultSummaryResponse> summaries = wishlistVaultRepository
                                .findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                                .map(this::toSummaryResponse)
                                .toList();

                return VaultListResponse.builder()
                                .vaults(summaries)
                                .build();
        }

        public VaultSummaryResponse getVaultDetail(Long vaultId, VaultKind kind) {
                Long userId = currentUserProvider.getCurrentUserId();

                if (kind == VaultKind.DEFAULT) {
                        DefaultVault vault = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        return toSummaryResponse(vault);
                } else {
                        WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        return toSummaryResponse(vault);
                }
        }

        public List<VaultHistoryResponse> getVaultHistory(Long vaultId, VaultKind kind) {
                Long userId = currentUserProvider.getCurrentUserId();

                // 권한 확인
                if (kind == VaultKind.DEFAULT) {
                        defaultVaultRepository.findByUserId(userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                } else {
                        wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                }

                return vaultHistoryRepository.findAllByVaultIdAndVaultKindOrderByCreatedAtDesc(vaultId, kind).stream()
                                .map(history -> VaultHistoryResponse.builder()
                                                .id(history.getId())
                                                .vaultId(history.getVaultId())
                                                .transactionType(history.getTransactionType().name())
                                                .amount(history.getAmount())
                                                .interestPaid(history.getInterestPaid())
                                                .createdAt(history.getCreatedAt())
                                                .build())
                                .toList();
        }

        private VaultSummaryResponse toSummaryResponse(DefaultVault vault) {
                return VaultSummaryResponse.builder()
                                .vaultId(vault.getId())
                                .vaultKind(VaultKind.DEFAULT.name())
                                .lockedAmount(vault.getLockedAmount())
                                .status(vault.getStatus().name())
                                .wishName("기본 금고")
                                .goalAmount(0L)
                                .baseInterestRate(vault.getBaseInterestRate())
                                .penaltyInterestRate(vault.getPenaltyInterestRate())
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }

        private VaultSummaryResponse toSummaryResponse(WishlistVault vault) {
                return VaultSummaryResponse.builder()
                                .vaultId(vault.getId())
                                .vaultKind(VaultKind.WISHLIST.name())
                                .lockedAmount(vault.getLockedAmount())
                                .status(vault.getStatus().name())
                                .wishName(vault.getWishName())
                                .goalAmount(vault.getGoalAmount())
                                .baseInterestRate(vault.getBaseInterestRate())
                                .penaltyInterestRate(vault.getPenaltyInterestRate())
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }
}