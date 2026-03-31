package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.DefaultVault;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultVaultRepository extends JpaRepository<DefaultVault, Long> {
    Optional<DefaultVault> findByUserId(Long userId);

    Optional<DefaultVault> findByUserIdAndStatus(Long userId, VaultStatus status);

    List<DefaultVault> findAllByStatus(VaultStatus status);

    boolean existsByUserIdAndStatus(Long userId, VaultStatus status);
}
