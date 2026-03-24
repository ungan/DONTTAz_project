package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.DefaultVault;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultWithdrawResponse;
import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;
import com.donttaz.donttazapi.memorandum.service.MemorandumService;
import com.donttaz.donttazapi.vault.domain.TransactionType;
import com.donttaz.donttazapi.vault.domain.VaultHistory;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent;
import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.donttaz.donttazapi.vault.repository.DefaultVaultRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
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
        private final MemorandumService memorandumService;
        private final RabbitTemplate rabbitTemplate;
        private final AccountRepository accountRepository;

        @Value("${vault.admin-user-id}")
        private Long adminUserId;

        @Value("${vault.admin-account-no}")
        private String adminAccountNo;

        public VaultCommandService(
                        DefaultVaultRepository defaultVaultRepository,
                        WishlistVaultRepository wishlistVaultRepository,
                        VaultHistoryRepository vaultHistoryRepository,
                        BankingClient bankingClient,
                        CurrentUserProvider currentUserProvider,
                        MemorandumService memorandumService,
                        RabbitTemplate rabbitTemplate,
                        AccountRepository accountRepository) {
                this.defaultVaultRepository = defaultVaultRepository;
                this.wishlistVaultRepository = wishlistVaultRepository;
                this.vaultHistoryRepository = vaultHistoryRepository;
                this.bankingClient = bankingClient;
                this.currentUserProvider = currentUserProvider;
                this.memorandumService = memorandumService;
                this.rabbitTemplate = rabbitTemplate;
                this.accountRepository = accountRepository;
        }

        @Transactional
        public CreateVaultResponse createDefaultVault() {
                Long userId = currentUserProvider.getCurrentUserId();
                createDefaultVault(userId);
                DefaultVault vault = defaultVaultRepository.findByUserId(userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                return CreateVaultResponse.builder()
                                .vaultId(vault.getId())
                                .vaultKind(VaultKind.DEFAULT.name())
                                .lockedAmount(vault.getLockedAmount())
                                .status(vault.getStatus().name())
                                .build();
        }

        @Transactional
        public CreateVaultResponse createWishlistVault(
                        com.donttaz.donttazapi.vault.dto.request.CreateWishlistVaultRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();
                validateWishlistUniqueness(userId);

                WishlistVault wishlistVault = WishlistVault.builder()
                                .userId(userId)
                                .wishName(request.wishName())
                                .goalAmount(request.goalAmount())
                                .status(VaultStatus.LOCKED)
                                .build();

                WishlistVault saved = wishlistVaultRepository.save(wishlistVault);

                memorandumService.createInitialMemorandum(
                                userId,
                                saved.getId());

                MemorandumResponse memoResponse = memorandumService.getMemorandumByVaultId(saved.getId());
                return CreateVaultResponse.fromWishlist(saved, memoResponse);
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

        @Transactional
        public VaultDepositResponse deposit(Long vaultId, VaultKind kind,
                        com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();

                // 대표계좌 조회 (없을 경우 첫 번째 계좌를 Fallback으로 사용)
                com.donttaz.donttazapi.account.domain.Account primaryAccount = accountRepository
                                .findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(userId,
                                                com.donttaz.donttazapi.account.domain.AccountStatus.ACTIVE)
                                .or(() -> {
                                        List<com.donttaz.donttazapi.account.domain.Account> accounts = accountRepository
                                                        .findAllByUserIdAndStatusAndIsDeletedFalse(userId,
                                                                        com.donttaz.donttazapi.account.domain.AccountStatus.ACTIVE);
                                        return accounts.isEmpty() ? java.util.Optional.empty()
                                                        : java.util.Optional.of(accounts.get(0));
                                })
                                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND,
                                                "사용 가능한 활동 계좌가 하나도 없습니다."));

                String accountNo = primaryAccount.getAccountNumber();
                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;

                if (kind == VaultKind.DEFAULT) {
                        DefaultVault vault = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        depositToDefault(vault, request.amount(), accountNo, targetAdminAccountNo);
                        return VaultDepositResponse.builder()
                                        .vaultId(vault.getId())
                                        .depositedAmount(request.amount())
                                        .lockedAmount(vault.getLockedAmount())
                                        .build();
                } else {
                        WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                        if (vault.getLockedAmount() + request.amount() > vault.getGoalAmount()) {
                                throw new CustomException(ErrorCode.INVALID_REQUEST,
                                                "입금 이후 금액이 위시리스트 목표 금액을 초과할 수 없습니다.");
                        }

                        depositToWishlist(vault, request.amount(), accountNo, targetAdminAccountNo);
                        return VaultDepositResponse.builder()
                                        .vaultId(vault.getId())
                                        .depositedAmount(request.amount())
                                        .lockedAmount(vault.getLockedAmount())
                                        .build();
                }
        }

        private void depositToDefault(DefaultVault vault, Long amount, String accountNo, String targetAdminAccountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                targetAdminAccountNo, "금고 입금 수납", String.valueOf(amount), accountNo, "금고 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.DEFAULT, TransactionType.DEPOSIT, amount, 0L);
        }

        private void depositToWishlist(WishlistVault vault, Long amount, String accountNo,
                        String targetAdminAccountNo) {
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                targetAdminAccountNo, "금고 입금 수납", String.valueOf(amount), accountNo, "금고 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.DEPOSIT, amount, 0L);

                if (vault.getLockedAmount() >= vault.getGoalAmount() && vault.getStatus() == VaultStatus.LOCKED) {
                        processWishlistMaturity(vault, vault.getUserId());
                }
        }

        @Transactional
        public void depositInternal(Long userId, String accountNo, Long amount) {
                List<WishlistVault> wishlistVaults = wishlistVaultRepository.findAllByUserIdAndStatus(userId,
                                VaultStatus.LOCKED);

                if (!wishlistVaults.isEmpty()) {
                        WishlistVault target = wishlistVaults.get(0);
                        long currentLocked = target.getLockedAmount();
                        long goalAmount = target.getGoalAmount();
                        long remainingCapacity = goalAmount - currentLocked;

                        if (remainingCapacity > 0) {
                                if (amount <= remainingCapacity) {
                                        depositToWishlistInternal(target, amount, accountNo);
                                } else {
                                        // 목표 금액만큼만 위시리스트 금고에 입금
                                        depositToWishlistInternal(target, remainingCapacity, accountNo);
                                        // 초과 금액은 기본 금고에 입금
                                        long excess = amount - remainingCapacity;
                                        DefaultVault defaultVault = defaultVaultRepository
                                                        .findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                                        .orElseThrow(() -> new CustomException(
                                                                        ErrorCode.VAULT_NOT_FOUND));
                                        depositToDefaultInternal(defaultVault, excess, accountNo);
                                }
                        } else {
                                // 이미 목표 금액을 달성했거나 초과한 경우 모두 기본 금고로
                                DefaultVault defaultVault = defaultVaultRepository
                                                .findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                                depositToDefaultInternal(defaultVault, amount, accountNo);
                        }
                } else {
                        DefaultVault target = defaultVaultRepository.findByUserIdAndStatus(userId, VaultStatus.LOCKED)
                                        .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));
                        depositToDefaultInternal(target, amount, accountNo);
                }
        }

        private void depositToWishlistInternal(WishlistVault vault, Long amount, String accountNo) {
                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                targetAdminAccountNo, "금고 자동 입금(배치)", String.valueOf(amount), accountNo, "금고 자동 입금"));
                vault.deposit(amount);
                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.DEPOSIT, amount, 0L);

                if (vault.getLockedAmount() >= vault.getGoalAmount() && vault.getStatus() == VaultStatus.LOCKED) {
                        processWishlistMaturity(vault, vault.getUserId());
                }
        }

        private void depositToDefaultInternal(DefaultVault vault, Long amount, String accountNo) {
                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;
                bankingClient.transfer(vault.getUserId(), new TransferCommand(
                                targetAdminAccountNo, "금고 자동 입금(배치)", String.valueOf(amount), accountNo, "금고 자동 입금"));
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

                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;
                Long targetAdminUserId = (adminUserId == null) ? 1L : adminUserId;

                bankingClient.transfer(targetAdminUserId, new TransferCommand(
                                request.accountNo(), "금고 출금 지급", String.valueOf(totalTransferAmount),
                                targetAdminAccountNo,
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

                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;
                Long targetAdminUserId = (adminUserId == null) ? 1L : adminUserId;

                bankingClient.transfer(targetAdminUserId, new TransferCommand(
                                request.accountNo(), "금고 출금 지급", String.valueOf(totalTransferAmount),
                                targetAdminAccountNo,
                                "금고 출금"));

                saveHistory(vault.getId(), VaultKind.WISHLIST, TransactionType.WITHDRAW_BROKEN, request.amount(),
                                interest);

                // 각서 노출 로직 (일부 인출 vs 전체 해지)
                if (vault.getStatus() == VaultStatus.BROKEN || (vault.getLockedAmount() == 0)) {
                        memorandumService.exposeByTerminate(vault.getId());
                } else {
                        memorandumService.exposeByWithdraw(vault.getId());
                }

                // RabbitMQ 팬아웃 (수치심 메시지 비동기 처리)
                rabbitTemplate.convertAndSend(RabbitMQConfig.PENALTY_EXCHANGE, RabbitMQConfig.PENALTY_ROUTING_KEY,
                                new VaultPenaltyEvent(vault.getId()));

                return VaultWithdrawResponse.builder()
                                .vaultId(vault.getId())
                                .withdrawnAmount(request.amount())
                                .interestPaid(interest)
                                .totalRefundAmount(totalTransferAmount)
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }

        @Transactional
        public void achieveWishlist(Long vaultId) {
                Long userId = currentUserProvider.getCurrentUserId();
                WishlistVault vault = wishlistVaultRepository.findByIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                processWishlistMaturity(vault, userId);
        }

        private void processWishlistMaturity(WishlistVault vault, Long userId) {
                Long interest = vault.calculateInterest();
                Long finalAmount = vault.getLockedAmount();
                vault.matureVault();
                Long totalTransferAmount = finalAmount + interest;

                Account primaryAccount = accountRepository
                                .findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(userId, AccountStatus.ACTIVE)
                                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

                String targetAdminAccountNo = (adminAccountNo == null || adminAccountNo.isBlank()) ? "9993479974590214"
                                : adminAccountNo;
                Long targetAdminUserId = (adminUserId == null) ? 1L : adminUserId;

                bankingClient.transfer(targetAdminUserId, new TransferCommand(
                                primaryAccount.getAccountNumber(), "금고 만기 환급", String.valueOf(totalTransferAmount),
                                targetAdminAccountNo,
                                "금고 자동 해지"));

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
