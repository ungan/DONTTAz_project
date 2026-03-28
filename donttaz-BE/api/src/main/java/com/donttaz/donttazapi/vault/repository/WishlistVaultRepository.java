package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistVaultRepository extends JpaRepository<WishlistVault, Long> {
    Optional<WishlistVault> findByIdAndUserId(Long id, Long userId);

    List<WishlistVault> findAllByUserIdAndStatus(Long userId, VaultStatus status);
    
    List<WishlistVault> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<WishlistVault> findAllByStatus(VaultStatus status);

    boolean existsByUserIdAndStatus(Long userId, VaultStatus status);
}
