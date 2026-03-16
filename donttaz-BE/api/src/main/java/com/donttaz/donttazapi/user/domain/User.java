package com.donttaz.donttazapi.user.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String pinCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    private String ci;

    private String deviceId;

    private String ssafyUserKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    @Builder
    public User(Long userId, String email, String password, String pinCode, String name, String phoneNumber, 
                String ci, String deviceId, String ssafyUserKey, UserStatus status) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.pinCode = pinCode;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
        this.deviceId = deviceId;
        this.ssafyUserKey = ssafyUserKey;
        this.status = status != null ? status : UserStatus.ACTIVE;
    }
}
