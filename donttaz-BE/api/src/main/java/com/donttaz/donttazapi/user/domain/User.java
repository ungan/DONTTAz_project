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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLDelete(sql = "UPDATE users SET is_deleted = true WHERE user_id = ?")
@SQLRestriction("is_deleted = false")
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

    private String nickname; // 신규 추가

    private String deviceId;

    private String ssafyUserKey;

    private String withdrawReason; // 신규 추가 (탈퇴 사유)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column
    private Integer salaryDay; // 급여일 (1~31)

    @Column
    private String salaryName; // 급여명 (식별용)

    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateSalaryInfo(Integer salaryDay, String salaryName) {
        this.salaryDay = salaryDay;
        this.salaryName = salaryName;
    }

    public void withdraw(String reason) {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawReason = reason;
    }

    @Builder
    public User(Long userId, String email, String password, String pinCode, String name, String phoneNumber,
            String ci, String nickname, String deviceId, String ssafyUserKey, UserStatus status,
            Integer salaryDay, String salaryName) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.pinCode = pinCode;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.ci = ci;
        this.nickname = nickname;
        this.deviceId = deviceId;
        this.ssafyUserKey = ssafyUserKey;
        this.status = status != null ? status : UserStatus.ACTIVE;
        this.salaryDay = salaryDay;
        this.salaryName = salaryName;
    }
}
