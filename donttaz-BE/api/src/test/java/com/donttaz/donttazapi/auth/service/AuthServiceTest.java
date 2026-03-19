package com.donttaz.donttazapi.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.auth.dto.request.RefreshRequest;
import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@ssafy.com";
    private static final String TEST_NAME = "김싸피";
    private static final String TEST_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 발급하고 Redis에 Refresh Token을 저장한다.")
    void login_Success() {
        // [Given]
        User user = createTestUser();
        LoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(jwtProvider.createAccessToken(TEST_USER_ID, TEST_EMAIL)).willReturn(ACCESS_TOKEN);
        given(jwtProvider.createRefreshToken(TEST_USER_ID, TEST_EMAIL)).willReturn(REFRESH_TOKEN);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // [When]
        LoginResponse response = authService.login(request);

        // [Then]
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);

        verify(valueOperations).set("RT:" + TEST_USER_ID, REFRESH_TOKEN, 14, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 USER_NOT_FOUND 예외가 발생한다.")
    void login_Fail_UserNotFound() {
        // [Given]
        LoginRequest request = createLoginRequest("wrong@ssafy.com", TEST_PASSWORD);
        given(userRepository.findByEmail("wrong@ssafy.com")).willReturn(Optional.empty());

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("비밀번호 불일치 시 INVALID_PASSWORD 예외가 발생한다.")
    void login_Fail_InvalidPassword() {
        // [Given]
        User user = createTestUser();
        LoginRequest request = createLoginRequest(TEST_EMAIL, "wrong-password");

        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", ENCODED_PASSWORD)).willReturn(false);

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @DisplayName("로그아웃 성공 시 Redis에서 Refresh Token을 삭제한다.")
    void logout_Success() {
        // [Given]
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(REFRESH_TOKEN);
        LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);

        // [When]
        authService.logout(request, TEST_USER_ID);

        // [Then]
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("로그아웃 시 Redis에 토큰이 없으면 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void logout_Fail_NoTokenInRedis() {
        // [Given]
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(null);
        LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.logout(request, TEST_USER_ID);
        });
    }

    @Test
    @DisplayName("로그아웃 시 요청된 토큰과 Redis의 토큰이 다르면 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void logout_Fail_TokenMismatch() {
        // [Given]
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(REFRESH_TOKEN);
        LogoutRequest request = new LogoutRequest("mismatch-token");

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.logout(request, TEST_USER_ID);
        });
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 액세스 토큰 재발급 성공 시 새로운 토큰 세트를 반환하고 Redis를 업데이트한다.")
    void refresh_Success() {
        // [Given]
        User user = createTestUser();
        RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        given(jwtProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + TEST_USER_ID)).willReturn(REFRESH_TOKEN);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        
        given(jwtProvider.createAccessToken(TEST_USER_ID, TEST_EMAIL)).willReturn(newAccessToken);
        given(jwtProvider.createRefreshToken(TEST_USER_ID, TEST_EMAIL)).willReturn(newRefreshToken);

        // [When]
        LoginResponse response = authService.refresh(request);

        // [Then]
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
        verify(valueOperations).set("RT:" + TEST_USER_ID, newRefreshToken, 14, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void refresh_Fail_InvalidToken() {
        // [Given]
        RefreshRequest request = new RefreshRequest("invalid-token");
        given(jwtProvider.validateToken("invalid-token")).willReturn(false);

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.refresh(request);
        });
    }

    @Test
    @DisplayName("Redis에 저장된 토큰과 일치하지 않으면 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void refresh_Fail_TokenMismatch() {
        // [Given]
        RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);
        given(jwtProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + TEST_USER_ID)).willReturn("different-token");

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.refresh(request);
        });
    }

    @Test
    @DisplayName("토큰은 유효하나 사용자가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다.")
    void refresh_Fail_UserNotFound() {
        // [Given]
        RefreshRequest request = new RefreshRequest(REFRESH_TOKEN);
        given(jwtProvider.validateToken(REFRESH_TOKEN)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(REFRESH_TOKEN)).willReturn(TEST_USER_ID);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("RT:" + TEST_USER_ID)).willReturn(REFRESH_TOKEN);
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            authService.refresh(request);
        });
    }

    // --- Helper Methods ---

    private User createTestUser() {
        User user = User.builder()
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .name(TEST_NAME)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "userId", TEST_USER_ID);
        return user;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        return LoginRequest.builder()
                .loginId(email)
                .password(password)
                .build();
    }
}
