package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.VaultHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VaultHistoryRepository extends JpaRepository<VaultHistory, Long> {
    List<VaultHistory> findAllByVaultIdOrderByCreatedAtDesc(Long vaultId);
}
