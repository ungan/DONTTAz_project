package com.donttaz.donttazapi.auth.service;

import com.donttaz.donttazapi.auth.dto.PortOneIdentityResponse;
import com.donttaz.donttazapi.auth.dto.request.FindIdRequest;
import com.donttaz.donttazapi.auth.dto.request.IdentityVerificationRequest;
import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.request.PasswordResetRequest;
import com.donttaz.donttazapi.auth.dto.request.PasswordVerifyIdentityRequest;
import com.donttaz.donttazapi.auth.dto.response.FindIdResponse;
import com.donttaz.donttazapi.auth.dto.response.IdentityVerificationResponse;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.dto.response.PasswordResetTokenResponse;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    private static final String PW_RESET_PREFIX = "PW_RESET:";

    @org.springframework.beans.factory.annotation.Value("${portone.api-secret:}")
    private String apiSecret;

    @org.springframework.beans.factory.annotation.Value("${local.test-user.email}")
    private String testEmail;

    @org.springframework.beans.factory.annotation.Value("${local.test-user.password}")
    private String testPassword;

    @org.springframework.beans.factory.annotation.Value("${local.test-user.pin}")
    private String testPin;

    @org.springframework.beans.factory.annotation.Value("${local.test-user.user-key}")
    private String testUserKey;

    @Transactional
    public FindIdResponse findId(FindIdRequest request) {
        PortOneIdentityResponse result = fetchPortOneIdentity(request.getIdentityVerificationId());

        String ci = result.getVerifiedCustomer().getCi();
        User user = userRepository.findByCi(ci)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String maskedEmail = maskEmail(user.getEmail());
        String createdAtStr = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return FindIdResponse.builder()
                .maskedLoginId(maskedEmail)
                .createdAt(createdAtStr)
                .build();
    }

    @Transactional
    public PasswordResetTokenResponse verifyIdentityForPasswordReset(PasswordVerifyIdentityRequest request) {
        User user = userRepository.findByEmail(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PortOneIdentityResponse result = fetchPortOneIdentity(request.getIdentityVerificationId());

        String ciFromPortOne = result.getVerifiedCustomer().getCi();
        if (user.getCi() == null || !user.getCi().equals(ciFromPortOne)) {
            throw new CustomException(ErrorCode.IDENTITY_VERIFICATION_FAILED);
        }

        String resetToken = UUID.randomUUID().toString();
        String redisKey = PW_RESET_PREFIX + resetToken;
        redisTemplate.opsForValue().set(redisKey, user.getUserId().toString(), 5, TimeUnit.MINUTES);

        return PasswordResetTokenResponse.builder()
                .resetToken(resetToken)
                .build();
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String redisKey = PW_RESET_PREFIX + request.getResetToken();
        String userIdStr = (String) redisTemplate.opsForValue().get(redisKey);

        if (userIdStr == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        redisTemplate.delete(redisKey);
    }

    @Transactional
    public IdentityVerificationResponse verifyIdentity(IdentityVerificationRequest request) {
        PortOneIdentityResponse result = fetchPortOneIdentity(request.getIdentityVerificationId());

        PortOneIdentityResponse.VerifiedCustomer customer = result.getVerifiedCustomer();
        String ci = customer.getCi();

        Optional<User> existingUser = userRepository.findByCi(ci);
        boolean isRegistered = existingUser.isPresent();
        String maskedEmail = isRegistered ? maskEmail(existingUser.get().getEmail()) : null;

        return IdentityVerificationResponse.builder()
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .isRegistered(isRegistered)
                .maskedEmail(maskedEmail)
                .build();
    }

    private PortOneIdentityResponse fetchPortOneIdentity(String verificationId) {
        String url = "https://api.portone.io/identity-verifications/" + verificationId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PortOneIdentityResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, PortOneIdentityResponse.class);
            PortOneIdentityResponse result = response.getBody();

            if (result == null || !"VERIFIED".equals(result.getStatus())) {
                throw new CustomException(ErrorCode.IDENTITY_VERIFICATION_FAILED);
            }
            return result;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.IDENTITY_VERIFICATION_FAILED);
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String id = parts[0];
        String domain = parts[1];

        if (id.length() <= 2) return id + "***@" + domain;
        
        // 아이디가 5글자보다 길면 앞 2자리, 뒤 2자리를 보여줌
        if (id.length() > 5) {
            return id.substring(0, 2) + "****" + id.substring(id.length() - 2) + "@" + domain;
        }
        
        // 그 외에는 앞 2자리만 보여주고 뒤는 가림
        return id.substring(0, 2) + "****@" + domain;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        String redisKey = "RT:" + user.getUserId();
        try {
            redisTemplate.opsForValue().set(redisKey, refreshToken, 14, TimeUnit.DAYS);
        } catch (Exception e) {
            System.err.println(">>> [Login] Redis 저장 중 에러 발생: " + e.getMessage());
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }

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
                    .status(com.donttaz.donttazapi.user.domain.UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
        }

        LoginRequest request = LoginRequest.builder()
                .loginId(testEmail)
                .password(testPassword)
                .build();

        return login(request);
    }

    @Transactional
    public void logout(LogoutRequest request, Long userId) {
        String redisKey = "RT:" + userId;
        String savedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedRefreshToken == null || !savedRefreshToken.equals(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        redisTemplate.delete(redisKey);
    }
}
