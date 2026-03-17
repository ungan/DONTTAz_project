package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultRepository extends JpaRepository<Vault, Long> {

    // CLOSED가 아닌 금고 목록 조회 (LOCKED + UNLOCKED)
    List<Vault> findAllByUserIdAndStatusNot(Long userId, VaultStatus status);

    Optional<Vault> findByVaultIdAndUserId(Long vaultId, Long userId);

    boolean existsByUserIdAndVaultKind(Long userId, VaultKind vaultKind);
}
