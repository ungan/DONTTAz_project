package com.donttaz.donttazapi.user.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fcm_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fcmTokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Builder
    public FcmToken(User user, String token, String deviceId, DeviceType deviceType) {
        this.user = user;
        this.token = token;
        this.deviceId = deviceId;
        this.deviceType = deviceType;
    }

    public void updateToken(String token) {
        this.token = token;
    }
}
