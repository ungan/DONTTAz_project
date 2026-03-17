package com.donttaz.donttazapi.user.repository;

import com.donttaz.donttazapi.user.domain.FcmToken;
import com.donttaz.donttazapi.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByDeviceId(String deviceId);
    void deleteByUser(User user);
}
