package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.response.VaultListResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import java.util.List;
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
    private final CurrentUserProvider currentUserProvider;

    public VaultQueryService(VaultRepository vaultRepository, CurrentUserProvider currentUserProvider) {
        this.vaultRepository = vaultRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * 현재 로그인한 사용자의 모든 활성 금고(종료되지 않은 금고) 목록을 조회합니다.
     *
     * @return 조회된 금고들의 요약 정보 목록 응답
     */
    public VaultListResponse getMyVaults() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<Vault> vaults = vaultRepository.findAllByUserIdAndStatusNot(userId, VaultStatus.CLOSED);

        List<VaultSummaryResponse> summaries = vaults.stream()
                .map(this::toVaultSummaryResponse)
                .toList();

        return VaultListResponse.builder()
                .vaults(summaries)
                .build();
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

        return toVaultSummaryResponse(vault);
    }

    private VaultSummaryResponse toVaultSummaryResponse(Vault vault) {
        return VaultSummaryResponse.builder()
                .vaultId(vault.getVaultId())
                .vaultKind(vault.getVaultKind().name())
                .balance(vault.getBalance())
                .status(vault.getStatus().name())
                .wishlistName(vault.getWishlistName())
                .goalAmount(vault.getGoalAmount())
                .goalDate(vault.getGoalDate())
                .isAchieved(vault.getIsAchieved())
                .build();
    }
}