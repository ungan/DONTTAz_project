package com.donttaz.donttazapi.memorandum.repository;

import com.donttaz.donttazapi.memorandum.domain.Memorandum;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemorandumRepository extends JpaRepository<Memorandum, Long> {
    Optional<Memorandum> findByWishlistVaultId(Long wishlistVaultId);
}
