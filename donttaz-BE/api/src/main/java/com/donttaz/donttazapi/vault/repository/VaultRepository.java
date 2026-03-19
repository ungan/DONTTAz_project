package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultRepository extends JpaRepository<Vault, Long> {

    Optional<Vault> findByVaultIdAndUserId(Long vaultId, Long userId);

    List<Vault> findAllByUserIdAndStatus(Long userId, VaultStatus status);

    boolean existsByUserIdAndVaultKindAndStatus(Long userId, VaultKind vaultKind, VaultStatus status);
}
