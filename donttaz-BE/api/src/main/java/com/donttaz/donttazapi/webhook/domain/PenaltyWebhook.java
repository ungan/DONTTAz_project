package com.donttaz.donttazapi.webhook.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String webhookToken; // Access Token

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    @Column(name = "receiver_uuids", length = 1000)
    private String receiverUuuids; // JSON Array String: ["uuid1", "uuid2"]

    @Column(name = "shame_message", nullable = false, length = 200)
    private String shameMessage;

    @Builder
    private PenaltyWebhook(Long userId, Long wishlistVaultId, PlatformType platformType, String webhookToken,
            String refreshToken, LocalDateTime accessTokenExpiresAt, String receiverUuuids, String shameMessage) {
        this.userId = userId;
        this.wishlistVaultId = wishlistVaultId;
        this.platformType = platformType;
        this.webhookToken = webhookToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.receiverUuuids = receiverUuuids;
        this.shameMessage = shameMessage;
    }

    public void updateWebhookSettings(PlatformType platformType, String webhookToken, String refreshToken,
            LocalDateTime accessTokenExpiresAt, String receiverUuuids, String shameMessage) {
        this.platformType = platformType;
        this.webhookToken = webhookToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.receiverUuuids = receiverUuuids;
        this.shameMessage = shameMessage;
    }

    public void updateAccessToken(String accessToken, LocalDateTime expiresAt) {
        this.webhookToken = accessToken;
        this.accessTokenExpiresAt = expiresAt;
    }
}
