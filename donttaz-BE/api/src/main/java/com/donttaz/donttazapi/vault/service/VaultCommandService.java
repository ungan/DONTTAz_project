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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CurrentUserProvider currentUserProvider;

    public VaultCommandService(
            VaultRepository vaultRepository,
            VaultHistoryRepository vaultHistoryRepository,
            CurrentUserProvider currentUserProvider) {
        this.vaultRepository = vaultRepository;
        this.vaultHistoryRepository = vaultHistoryRepository;
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

        if (request.vaultKind() == VaultKind.DEFAULT) {
            validateDefaultVaultUniqueness(userId);
        } else {
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
        if (vaultRepository.existsByUserIdAndVaultKindAndStatus(userId, VaultKind.DEFAULT, VaultStatus.LOCKED)) {
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

    private void validateDefaultVaultUniqueness(Long userId) {
        if (vaultRepository.existsByUserIdAndVaultKindAndStatus(userId, VaultKind.DEFAULT, VaultStatus.LOCKED)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "기본 금고는 사용자당 1개만 생성할 수 있습니다.");
        }
    }

    private void validateWishlistRequirement(CreateVaultRequest request) {
        if (request.wishName() == null || request.wishName().isBlank() ||
                request.goalAmount() == null || request.targetDate() == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "목표 금고 생성 시 목표 이름, 금액, 날짜는 필수입니다.");
        }
    }

    @Transactional
    public VaultDepositResponse deposit(Long vaultId, Long amount) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

        // 자산 이체 및 금고 잠금 처리 (금융 연동 예정)
        vault.deposit(amount);

        vaultHistoryRepository.save(VaultHistory.builder()
                .vaultId(vaultId)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .interestPaid(0L)
                .build());

        return VaultDepositResponse.builder()
                .vaultId(vault.getVaultId())
                .depositedAmount(amount)
                .lockedAmount(vault.getLockedAmount())
                .build();
    }

    @Transactional
    public VaultWithdrawResponse withdraw(Long vaultId, Long amount) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

        // 1. 현재 시점 이자 계산 (금액 비례가 아닌 전체 잔액 기준 이자 지급으로 가정)
        Long interest = vault.calculateInterest();

        // 2. 출금 로직 실행 (패널티 누적 및 복구 반영)
        vault.withdraw(amount);

        // 3. 거래 내역 저장
        vaultHistoryRepository.save(VaultHistory.builder()
                .vaultId(vaultId)
                .transactionType(TransactionType.WITHDRAW_BROKEN)
                .amount(amount)
                .interestPaid(interest)
                .build());

        return VaultWithdrawResponse.builder()
                .vaultId(vault.getVaultId())
                .withdrawnAmount(amount)
                .interestPaid(interest)
                .totalRefundAmount(amount + interest)
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
