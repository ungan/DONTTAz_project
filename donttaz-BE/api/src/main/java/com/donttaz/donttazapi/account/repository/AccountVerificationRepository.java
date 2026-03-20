package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.AccountVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification, String> {

    Optional<AccountVerification> findByVerificationIdAndUserId(String verificationId, Long userId);

    Optional<AccountVerification> findByConsentTokenAndVerifiedTrue(String consentToken);
}
