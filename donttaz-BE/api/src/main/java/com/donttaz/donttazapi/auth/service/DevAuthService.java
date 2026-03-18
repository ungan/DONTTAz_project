package com.donttaz.donttazapi.auth.service;

import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Profile({ "local", "dev" })
@RequiredArgsConstructor
public class DevAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${local.test-user.email:}")
    private String testEmail;

    @Value("${local.test-user.password:}")
    private String testPassword;

    @Value("${local.test-user.pin:}")
    private String testPin;

    @Value("${local.test-user.user-key:}")
    private String testUserKey;

    @Transactional
    public LoginResponse testLogin() {
        if (!userRepository.existsByEmail(testEmail)) {
            User user = User.builder()
                    .email(testEmail)
                    .password(passwordEncoder.encode(testPassword))
                    .pinCode(passwordEncoder.encode(testPin))
                    .name("테스트유저")
                    .phoneNumber("01012345678")
                    .ssafyUserKey(testUserKey)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
        }

        User user = userRepository.findByEmail(testEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(testPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        String redisKey = "RT:" + user.getUserId();
        try {
            redisTemplate.opsForValue().set(redisKey, refreshToken, 14, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println(">>> [DevLogin] Redis 저장 중 에러 발생: " + e.getMessage());
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }
}
