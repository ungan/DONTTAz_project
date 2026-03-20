package com.donttaz.donttazapi.vault.repository;

import com.donttaz.donttazapi.vault.domain.VaultHistory;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VaultHistoryRepository extends JpaRepository<VaultHistory, Long> {
    List<VaultHistory> findAllByVaultIdAndVaultKindOrderByCreatedAtDesc(Long vaultId, VaultKind vaultKind);
}
