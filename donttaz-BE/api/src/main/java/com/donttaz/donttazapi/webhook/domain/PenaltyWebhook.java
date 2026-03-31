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

    @Column(name = "shame_message", nullable = false, length = 200)
    private String shameMessage;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Builder
    private PenaltyWebhook(Long userId, Long wishlistVaultId, PlatformType platformType,
            String shameMessage, String webhookUrl) {
        this.userId = userId;
        this.wishlistVaultId = wishlistVaultId;
        this.platformType = platformType;
        this.shameMessage = shameMessage;
        this.webhookUrl = webhookUrl;
    }

    public void updateWebhookSettings(PlatformType platformType, String shameMessage, String webhookUrl) {
        this.platformType = platformType;
        this.shameMessage = shameMessage;
        this.webhookUrl = webhookUrl;
    }
}
