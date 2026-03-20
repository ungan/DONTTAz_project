package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.DefaultVault;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.request.SetWishlistGoalRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultWithdrawResponse;
import com.donttaz.donttazapi.vault.domain.TransactionType;
import com.donttaz.donttazapi.vault.domain.VaultHistory;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.vault.repository.DefaultVaultRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;

@Service
@Transactional
public class VaultCommandService {

        private final DefaultVaultRepository defaultVaultRepository;
        private final WishlistVaultRepository wishlistVaultRepository;
        private final VaultHistoryRepository vaultHistoryRepository;
        private final BankingClient bankingClient;
        private final CurrentUserProvider currentUserProvider;

        @Value("${vault.admin-user-id}")
        private Long adminUserId;

        @Value("${vault.admin-account-no}")
        private String adminAccountNo;

        public VaultCommandService(
                        DefaultVaultRepository defaultVaultRepository,
                        WishlistVaultRepository wishlistVaultRepository,
                        VaultHistoryRepository vaultHistoryRepository,
                        BankingClient bankingClient,
                        CurrentUserProvider currentUserProvider) {
                this.defaultVaultRepository = defaultVaultRepository;
                this.wishlistVaultRepository = wishlistVaultRepository;
                this.vaultHistoryRepository = vaultHistoryRepository;
                this.bankingClient = bankingClient;
                this.currentUserProvider = currentUserProvider;
        }

        @Transactional
        public CreateVaultResponse createVault(CreateVaultRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();

                if (request.vaultKind() == VaultKind.DEFAULT) {
                        createDefaultVault(userId);
                        DefaultVault vault = defaultVaultRepository.findByUserId(userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        return CreateVaultResponse.builder()
                                        .vaultId(vault.getId())
                                        .lockedAmount(vault.getLockedAmount())
                                        .status(vault.getStatus().name())
                                        .build();
                }

                validateWishlistUniqueness(userId);
                validateWishlistRequirement(request);

                WishlistVault vault = WishlistVault.builder()
                                .userId(userId)
                                .wishName(request.wishName())
                                .goalAmount(request.goalAmount())
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();

                WishlistVault savedVault = wishlistVaultRepository.save(vault);

                return CreateVaultResponse.builder()
                                .vaultId(savedVault.getId())
                                .lockedAmount(savedVault.getLockedAmount())
                                .status(savedVault.getStatus().name())
                                .build();
        }

        @Transactional
        public void createDefaultVault(Long userId) {
                if (defaultVaultRepository.existsByUserIdAndStatus(userId, VaultStatus.LOCKED)) {
                        return;
                }

                DefaultVault vault = DefaultVault.builder()
                                .userId(userId)
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();

                defaultVaultRepository.save(vault);
        }

        private void validateWishlistUniqueness(Long userId) {
                if (wishlistVaultRepository.existsByUserIdAndStatus(userId, VaultStatus.LOCKED)) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST, "사용자는 1개의 활성 위시리스트 금고만 가질 수 있습니다.");
                }
        }

        private void validateWishlistRequirement(CreateVaultRequest request) {
                if (request.wishName() == null || request.wishName().isBlank() || request.goalAmount() == null) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST, "위시리스트 금고 생성 시 목표 이름과 금액은 필수입니다.");
                }
        }

        @Transactional
        public VaultDepositResponse deposit(Long vaultId, VaultKind kind,
                        com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();

                if (kind == VaultKind.DEFAULT) {
                        DefaultVault vault = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        depositToDefault(vault, request.amount(), request.accountNo());
                        return VaultDepositResponse.builder()
                                        .vaultId(vault.getId())
                                        .depositedAmount(request.amount())
                                        .lockedAmount(vault.getLockedAmount())
                                        .build();
                } else {
                        WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        depositToWishlist(vault, request.amount(), request.accountNo());
                        return VaultDepositResponse.builder()
                                        .vaultId(vault.getId())
                                        .depositedAmount(request.amount())
                                        .lockedAmount(vault.getLockedAmount())
                                        .build();
                }
        }

        private void depositToDefault(DefaultVault vault, Long amount, String accountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                adminAccountNo, "금고 입금 수납", String.valueOf(amount), accountNo, "금고 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.DEFAULT, TransactionType.DEPOSIT, amount, 0L);
        }

        private void depositToWishlist(WishlistVault vault, Long amount, String accountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                adminAccountNo, "금고 입금 수납", String.valueOf(amount), accountNo, "금고 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.DEPOSIT, amount, 0L);
        }

        @Transactional
        public void depositInternal(Long userId, String accountNo, Long amount) {
                List<WishlistVault> wishlistVaults = wishlistVaultRepository.findAllByUserIdAndStatus(userId,
                                VaultStatus.LOCKED);

                if (!wishlistVaults.isEmpty()) {
                        WishlistVault target = wishlistVaults.get(0);
                        depositToWishlistInternal(target, amount, accountNo);
                } else {
                        DefaultVault target = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        depositToDefaultInternal(target, amount, accountNo);
                }
        }

        private void depositToWishlistInternal(WishlistVault vault, Long amount, String accountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                adminAccountNo, "금고 자동 입금(배치)", String.valueOf(amount), accountNo, "금고 자동 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.DEPOSIT, amount, 0L);
        }

        private void depositToDefaultInternal(DefaultVault vault, Long amount, String accountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                adminAccountNo, "금고 자동 입금(배치)", String.valueOf(amount), accountNo, "금고 자동 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.DEFAULT, TransactionType.DEPOSIT, amount, 0L);
        }

        @Transactional
        public VaultWithdrawResponse withdraw(Long vaultId, VaultKind kind,
                        com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();

                if (kind == VaultKind.DEFAULT) {
                        DefaultVault vault = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        return withdrawFromDefault(vault, request);
                } else {
                        WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        return withdrawFromWishlist(vault, request);
                }
        }

        private VaultWithdrawResponse withdrawFromDefault(DefaultVault vault,
                        com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest request) {
                Long interest = vault.calculateInterest();
                vault.withdraw(request.amount());
                Long totalTransferAmount = request.amount() + interest;

                bankingClient.transfer(adminUserId, new TransferCommand(
                                request.accountNo(), "금고 출금 지급", String.valueOf(totalTransferAmount), adminAccountNo,
                                "금고 출금"));

                saveHistory(vault.getId(), VaultKind.DEFAULT, TransactionType.WITHDRAW_BROKEN, request.amount(),
                                interest);

                return VaultWithdrawResponse.builder()
                                .vaultId(vault.getId())
                                .withdrawnAmount(request.amount())
                                .interestPaid(interest)
                                .totalRefundAmount(totalTransferAmount)
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }

        private VaultWithdrawResponse withdrawFromWishlist(WishlistVault vault,
                        com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest request) {
                Long interest = vault.calculateInterest();
                vault.withdraw(request.amount());
                Long totalTransferAmount = request.amount() + interest;

                bankingClient.transfer(adminUserId, new TransferCommand(
                                request.accountNo(), "금고 출금 지급", String.valueOf(totalTransferAmount), adminAccountNo,
                                "금고 출금"));

                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.WITHDRAW_BROKEN, request.amount(),
                                interest);

                return VaultWithdrawResponse.builder()
                                .vaultId(vault.getId())
                                .withdrawnAmount(request.amount())
                                .interestPaid(interest)
                                .totalRefundAmount(totalTransferAmount)
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }

        @Transactional
        public void setWishlistGoal(Long vaultId, SetWishlistGoalRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();
                WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                vault.updateGoal(request.wishName(), request.goalAmount());
        }

        @Transactional
        public void achieveWishlist(Long vaultId) {
                Long userId = currentUserProvider.getCurrentUserId();
                WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                Long interest = vault.calculateInterest();
                Long finalAmount = vault.getLockedAmount();
                vault.matureVault();

                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.WITHDRAW_MATURED, finalAmount, interest);
        }

        private void saveHistory(Long vaultId, VaultKind kind, TransactionType type, Long amount, Long interest) {
                vaultHistoryRepository.save(VaultHistory.builder()
                                .vaultId(vaultId)
                                .vaultKind(kind)
                                .transactionType(type)
                                .amount(amount)
                                .interestPaid(interest)
                                .build());
        }
}
