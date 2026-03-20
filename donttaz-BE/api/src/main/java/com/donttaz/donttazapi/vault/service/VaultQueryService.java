package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.response.VaultHistoryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultListResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 금고(Vault) 관련 데이터 조회(Query)를 전담하는 서비스.
 * 사용자 소유의 전체 금고 목록 조회 및 개별 금고 상세 조회를 처리합니다.
 * 상태 변경 책임이 있는 CommandService와 분리된 CQRS 패턴을 따릅니다.
 */
@Service
@Transactional(readOnly = true)
public class VaultQueryService {

        private final VaultRepository vaultRepository;
        private final VaultHistoryRepository vaultHistoryRepository;
        private final VaultCommandService vaultCommandService;
        private final CurrentUserProvider currentUserProvider;

        public VaultQueryService(VaultRepository vaultRepository,
                        VaultHistoryRepository vaultHistoryRepository,
                        @Lazy VaultCommandService vaultCommandService,
                        CurrentUserProvider currentUserProvider) {
                this.vaultRepository = vaultRepository;
                this.vaultHistoryRepository = vaultHistoryRepository;
                this.vaultCommandService = vaultCommandService;
                this.currentUserProvider = currentUserProvider;
        }

        /**
         * 현재 로그인한 사용자의 모든 활성 금고(종료되지 않은 금고) 목록을 조회합니다.
         * 최초 조회 시 기본 금고가 없다면 생성합니다.
         *
         * @return 조회된 금고들의 요약 정보 목록 응답
         */
        @Transactional
        public VaultListResponse getMyVaults() {
                Long userId = currentUserProvider.getCurrentUserId();

                // 기본 금고 존재 보장 (최초 사용 시 생성)
                vaultCommandService.createDefaultVault(userId);

                List<VaultSummaryResponse> summaries = getActiveVaults(userId);

                return VaultListResponse.builder()
                                .vaults(summaries)
                                .build();
        }

        public List<VaultSummaryResponse> getActiveVaults(Long userId) {
                return vaultRepository.findAllByUserIdAndStatus(userId, VaultStatus.LOCKED).stream()
                                .map(vault -> new VaultSummaryResponse(
                                                vault.getVaultId(),
                                                vault.getVaultKind().name(),
                                                vault.getLockedAmount(),
                                                vault.getStatus().name(),
                                                vault.getWishName(),
                                                vault.getGoalAmount(),
                                                vault.getTargetDate(),
                                                vault.getBaseInterestRate(),
                                                vault.getPenaltyInterestRate(),
                                                vault.getWithdrawalCount()))
                                .toList();
        }

        /**
         * 특정 금고의 상세 정보를 조회합니다.
         * 내 소유의 금고가 아니거나 존재하지 않는 금고인 경우 예외를 발생시킵니다.
         *
         * @param vaultId 상세 조회할 금고 ID
         * @return 금고의 상세(요약) 정보 응답
         * @throws CustomException 해당 금고를 찾을 수 없거나 접근 권한이 없을 경우
         */
        public VaultSummaryResponse getVaultDetail(Long vaultId) {
                Long userId = currentUserProvider.getCurrentUserId();
                Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                return new VaultSummaryResponse(
                                vault.getVaultId(),
                                vault.getVaultKind().name(),
                                vault.getLockedAmount(),
                                vault.getStatus().name(),
                                vault.getWishName(),
                                vault.getGoalAmount(),
                                vault.getTargetDate(),
                                vault.getBaseInterestRate(),
                                vault.getPenaltyInterestRate(),
                                vault.getWithdrawalCount());
        }

        /**
         * 특정 금고의 거래 내역 목록을 조회합니다.
         *
         * @param vaultId 내역을 조회할 금고 ID
         * @return 거래 내역 목록 응답
         */
        public List<VaultHistoryResponse> getVaultHistory(Long vaultId) {
                Long userId = currentUserProvider.getCurrentUserId();
                // 본인 금고인지 검증
                vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

                return vaultHistoryRepository.findAllByVaultIdOrderByCreatedAtDesc(vaultId).stream()
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
}