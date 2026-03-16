package com.donttaz.donttazapi.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
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
        // AuthService의 @Value 필드 설정 (단위 테스트 환경)
        ReflectionTestUtils.setField(authService, "testEmail", TEST_EMAIL);
        ReflectionTestUtils.setField(authService, "testPassword", TEST_PASSWORD);
        ReflectionTestUtils.setField(authService, "testPin", "123456");
        ReflectionTestUtils.setField(authService, "testUserKey", "ssafy-user-key");
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
    @DisplayName("테스트 자동 로그인 시 사용자가 이미 존재하면 기존 계정으로 로그인한다.")
    void testLogin_Success_WhenUserExists() {
        // [Given]
        User user = createTestUser();
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(true);
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(jwtProvider.createAccessToken(TEST_USER_ID, TEST_EMAIL)).willReturn(ACCESS_TOKEN);
        given(jwtProvider.createRefreshToken(TEST_USER_ID, TEST_EMAIL)).willReturn(REFRESH_TOKEN);
        
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // [When]
        LoginResponse response = authService.testLogin();

        // [Then]
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("테스트 자동 로그인 시 사용자가 존재하지 않으면 신규 생성 후 로그인한다.")
    void testLogin_Success_WhenUserNotExists() {
        // [Given]
        User user = createTestUser();
        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        
        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(jwtProvider.createAccessToken(TEST_USER_ID, TEST_EMAIL)).willReturn(ACCESS_TOKEN);
        given(jwtProvider.createRefreshToken(TEST_USER_ID, TEST_EMAIL)).willReturn(REFRESH_TOKEN);
        
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // [When]
        LoginResponse response = authService.testLogin();

        // [Then]
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        verify(userRepository).save(any(User.class));
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
