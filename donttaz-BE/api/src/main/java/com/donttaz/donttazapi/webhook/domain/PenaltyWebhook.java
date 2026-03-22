package com.donttaz.donttazapi.webhook.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "penalty_webhook")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PenaltyWebhook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penalty_webhook_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wishlist_vault_id", nullable = false)
    private Long wishlistVaultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", nullable = false, length = 30)
    private PlatformType platformType;

    @Column(name = "webhook_token", nullable = false, length = 500)
    private String webhookToken;

    @Column(name = "shame_message", nullable = false, length = 200)
    private String shameMessage;

    @Builder
    private PenaltyWebhook(Long userId, Long wishlistVaultId, PlatformType platformType, String webhookToken,
            String shameMessage) {
        this.userId = userId;
        this.wishlistVaultId = wishlistVaultId;
        this.platformType = platformType;
        this.webhookToken = webhookToken;
        this.shameMessage = shameMessage;
    }

    public void updateWebhookSettings(PlatformType platformType, String webhookToken, String shameMessage) {
        this.platformType = platformType;
        this.webhookToken = webhookToken;
        this.shameMessage = shameMessage;
    }
}
