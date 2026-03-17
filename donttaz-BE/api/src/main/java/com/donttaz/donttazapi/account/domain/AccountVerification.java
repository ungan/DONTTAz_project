package com.donttaz.donttazapi.account.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE account_verifications SET is_deleted = true WHERE verification_id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "account_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountVerification extends BaseEntity {

    @Id
    @Column(nullable = false, length = 50)
    private String verificationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String bankCode;

    @Column(nullable = false, length = 30)
    private String accountNumber;

    @Column(nullable = false, length = 10)
    private String authCode;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(length = 255)
    private String consentToken;

    @Builder
    private AccountVerification(String verificationId, Long userId, String bankCode, String accountNumber,
            String authCode, LocalDateTime expiredAt, boolean verified, String consentToken) {
        this.verificationId = verificationId;
        this.userId = userId;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.authCode = authCode;
        this.expiredAt = expiredAt;
        this.verified = verified;
        this.consentToken = consentToken;
    }
}
