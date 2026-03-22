package com.donttaz.donttazapi.webhook.repository;

import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PenaltyWebhookRepository extends JpaRepository<PenaltyWebhook, Long> {
    Optional<PenaltyWebhook> findByWishlistVaultId(Long wishlistVaultId);
}
