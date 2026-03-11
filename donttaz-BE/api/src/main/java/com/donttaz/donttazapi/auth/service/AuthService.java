package com.donttaz.donttazapi.auth.service;

import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId(), user.getEmail());

        // [진단 로그] Redis 저장 시도
        String redisKey = "RT:" + user.getUserId();
        System.out.println(">>> [Login] Redis 저장 시도 - Key: " + redisKey);
        
        try {
            redisTemplate.opsForValue().set(redisKey, refreshToken, 14, TimeUnit.DAYS);
            
            // [진단 로그] 저장 후 즉시 확인
            Object savedValue = redisTemplate.opsForValue().get(redisKey);
            System.out.println(">>> [Login] 저장 직후 Redis 확인 결과: " + savedValue);
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
    public void logout(LogoutRequest request, Long userId) {
        String redisKey = "RT:" + userId;
        System.out.println(">>> [Logout] Redis 조회 시도 - Key: " + redisKey);
        System.out.println(">>> [Logout] 요청받은 Token: " + request.getRefreshToken());

        String savedRefreshToken = (String) redisTemplate.opsForValue().get(redisKey);
        System.out.println(">>> [Logout] Redis에서 꺼낸 값: " + savedRefreshToken);

        if (savedRefreshToken == null) {
            System.err.println(">>> [Logout] 실패: Redis에 저장된 토큰이 없습니다 (null)");
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        
        if (!savedRefreshToken.equals(request.getRefreshToken())) {
            System.err.println(">>> [Logout] 실패: 토큰이 일치하지 않습니다.");
            System.err.println(">>> [Stored]: " + savedRefreshToken);
            System.err.println(">>> [Request]: " + request.getRefreshToken());
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        redisTemplate.delete(redisKey);
        System.out.println(">>> [Logout] 성공: Redis에서 토큰 삭제 완료");
    }
}
