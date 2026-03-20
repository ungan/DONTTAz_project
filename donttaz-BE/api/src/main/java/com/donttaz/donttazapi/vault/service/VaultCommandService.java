package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.Vault;
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
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;

/**
 * 금고(Vault) 관련 상태 변경(Command)을 처리하는 서비스.
 * 1. 계좌 생성 시 기본 금고(DEFAULT) 자동 생성 로직 연동
 * 2. 금고 종류별(DEFAULT, WISHLIST) 생성 유효성 검증 정책 적용
 * - DEFAULT: 사용자당 1개만 생성 가능
 * - WISHLIST: 목표 이름, 금액, 날짜 필드 필수
 */
@Service
@Transactional
public class VaultCommandService {

        private final VaultRepository vaultRepository;
        private final VaultHistoryRepository vaultHistoryRepository;
        private final BankingClient bankingClient;
        private final CurrentUserProvider currentUserProvider;

        @Value("${vault.admin-user-id}")
        private Long adminUserId;

        @Value("${vault.admin-account-no}")
        private String adminAccountNo;

        public VaultCommandService(
                        VaultRepository vaultRepository,
                        VaultHistoryRepository vaultHistoryRepository,
                        BankingClient bankingClient,
                        CurrentUserProvider currentUserProvider) {
                this.vaultRepository = vaultRepository;
                this.vaultHistoryRepository = vaultHistoryRepository;
                this.bankingClient = bankingClient;
                this.currentUserProvider = currentUserProvider;
        }

        /**
         * 새로운 금고(DEFAULT 또는 WISHLIST)를 생성합니다.
         * - DEFAULT: 사용자당 1개만 활성화 가능하며, 계좌 생성 시 자동 생성됩니다.
         * - WISHLIST: 목표 이름, 금액, 날짜가 필수인 목표 달성용 금고입니다.
         */
        @Transactional
        public CreateVaultResponse createVault(CreateVaultRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();

                validateVaultUniqueness(userId);

                if (request.vaultKind() == VaultKind.WISHLIST) {
                        validateWishlistRequirement(request);
                }

                Vault vault = Vault.builder()
                                .userId(userId)
                                .vaultKind(request.vaultKind())
                                .wishName(request.vaultKind() == VaultKind.DEFAULT ? "기본 금고" : request.wishName())
                                .goalAmount(request.goalAmount())
                                .targetDate(request.targetDate())
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();

                Vault savedVault = vaultRepository.save(vault);

                return CreateVaultResponse.builder()
                                .vaultId(savedVault.getVaultId())
                                .lockedAmount(savedVault.getLockedAmount())
                                .status(savedVault.getStatus().name())
                                .build();
        }

        /**
         * 계좌 생성 시 자동으로 호출되는 기본 금고 생성 메서드입니다.
         */
        @Transactional
        public void createDefaultVault(Long userId) {
                if (vaultRepository.existsByUserIdAndVaultKindAndStatus(userId, VaultKind.DEFAULT,
                                VaultStatus.LOCKED)) {
                        return; // 이미 존재하면 생성하지 않음 (멱등성 보장)
                }

                Vault vault = Vault.builder()
                                .userId(userId)
                                .vaultKind(VaultKind.DEFAULT)
                                .wishName("기본 금고")
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();

                vaultRepository.save(vault);
        }

        private void validateVaultUniqueness(Long userId) {
                if (!vaultRepository.findAllByUserIdAndStatus(userId, VaultStatus.LOCKED).isEmpty()) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST, "사용자는 1개의 활성 금고만 가질 수 있습니다.");
                }
        }

        private void validateWishlistRequirement(CreateVaultRequest request) {
                if (request.wishName() == null || request.wishName().isBlank() ||
                                request.goalAmount() == null || request.targetDate() == null) {
                        throw new CustomException(ErrorCode.INVALID_REQUEST, "목표 금고 생성 시 목표 이름, 금액, 날짜는 필수입니다.");
                }
        }

        @Transactional
        public VaultDepositResponse deposit(Long vaultId,
                        com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();
                Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                // 입금 로직: 사용자 계좌 -> 관리자 모계좌 이체
                bankingClient.transfer(userId, new TransferCommand(
                                adminAccountNo,
                                "금고 입금 수납",
                                String.valueOf(request.amount()),
                                request.accountNo(),
                                "금고 입금"));

                vault.deposit(request.amount());

                vaultHistoryRepository.save(VaultHistory.builder()
                                .vaultId(vaultId)
                                .transactionType(TransactionType.DEPOSIT)
                                .amount(request.amount())
                                .interestPaid(0L)
                                .build());

                return VaultDepositResponse.builder()
                                .vaultId(vault.getVaultId())
                                .depositedAmount(request.amount())
                                .lockedAmount(vault.getLockedAmount())
                                .build();
        }

        /**
         * 시스템(배치 등)에 의해 호출되는 내부 입금 메서드입니다.
         * CurrentUserProvider가 아닌 명시적인 userId를 기반으로 처리합니다.
         */
        @Transactional
        public void depositInternal(Long userId, String accountNo, Long amount) {
                // 1. 위시리스트 금고 최우선 (목표 달성을 위해), 없으면 기본 금고 사용
                List<Vault> lockedVaults = vaultRepository.findAllByUserIdAndStatus(userId, VaultStatus.LOCKED);

                Vault targetVault = lockedVaults.stream()
                                .filter(v -> v.getVaultKind() == VaultKind.WISHLIST)
                                .findFirst()
                                .orElseGet(() -> lockedVaults.stream()
                                                .filter(v -> v.getVaultKind() == VaultKind.DEFAULT)
                                                .findFirst()
                                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND)));

                // 사용자 계좌 -> 관리자 모계좌 이체
                bankingClient.transfer(userId, new TransferCommand(
                                adminAccountNo,
                                "금고 자동 입금(배치)",
                                String.valueOf(amount),
                                accountNo,
                                "금고 자동 입금"));

                targetVault.deposit(amount);

                vaultHistoryRepository.save(VaultHistory.builder()
                                .vaultId(targetVault.getVaultId())
                                .transactionType(TransactionType.DEPOSIT)
                                .amount(amount)
                                .interestPaid(0L)
                                .build());
        }

        @Transactional
        public VaultWithdrawResponse withdraw(Long vaultId,
                        com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();
                Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                // 1. 현재 시점 이자 계산
                Long interest = vault.calculateInterest();

                // 2. 출금 로직 실행 (패널티 누적 및 복구 반영)
                vault.withdraw(request.amount());

                // 3. 출금 로직: 관리자 모계좌 -> 사용자 계좌 이체
                Long totalTransferAmount = request.amount() + interest;
                bankingClient.transfer(adminUserId, new TransferCommand(
                                request.accountNo(),
                                "금고 출금 지급",
                                String.valueOf(totalTransferAmount),
                                adminAccountNo,
                                "금고 출금"));

                // 4. 거래 내역 저장
                vaultHistoryRepository.save(VaultHistory.builder()
                                .vaultId(vaultId)
                                .transactionType(TransactionType.WITHDRAW_BROKEN)
                                .amount(request.amount())
                                .interestPaid(interest)
                                .build());

                return VaultWithdrawResponse.builder()
                                .vaultId(vault.getVaultId())
                                .withdrawnAmount(request.amount())
                                .interestPaid(interest)
                                .totalRefundAmount(totalTransferAmount)
                                .withdrawalCount(vault.getWithdrawalCount())
                                .build();
        }

        /**
         * WISHLIST 금고의 위시리스트 목표 이름, 금액, 날짜를 설정하거나 수정합니다.
         */
        @Transactional
        public void setWishlistGoal(Long vaultId, SetWishlistGoalRequest request) {
                Long userId = currentUserProvider.getCurrentUserId();
                Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                vault.updateGoal(request.wishName(), request.goalAmount(), request.targetDate());
        }

        @Transactional
        public void achieveWishlist(Long vaultId) {
                Long userId = currentUserProvider.getCurrentUserId();
                Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                Long interest = vault.calculateInterest();
                Long finalAmount = vault.getLockedAmount();

                vault.matureVault();

                vaultHistoryRepository.save(VaultHistory.builder()
                                .vaultId(vaultId)
                                .transactionType(TransactionType.WITHDRAW_MATURED)
                                .amount(finalAmount)
                                .interestPaid(interest)
                                .build());
        }
}
