package com.donttaz.donttazapi.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
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
        // 공통 설정에서는 Redis 관련 stubbing을 제거합니다.
        // 모든 테스트에서 Redis를 사용하는 것이 아니므로 UnnecessaryStubbingException이 발생할 수 있기 때문입니다.
    }

    @Test
    @DisplayName("로그인 성공 시 토큰을 발급하고 Redis에 Refresh Token을 저장한다.")
    void login_Success() {
        // Given
        User user = createTestUser();
        LoginRequest request = createLoginRequest(TEST_EMAIL, TEST_PASSWORD);

        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(jwtProvider.createAccessToken(TEST_USER_ID, TEST_EMAIL)).willReturn(ACCESS_TOKEN);
        given(jwtProvider.createRefreshToken(TEST_USER_ID, TEST_EMAIL)).willReturn(REFRESH_TOKEN);
        
        // 해당 테스트에서만 필요한 Redis stubbing 정의
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        // 진단 로그용 get() stubbing 추가
        given(valueOperations.get("RT:" + TEST_USER_ID)).willReturn(REFRESH_TOKEN);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);

        // Redis 저장 검증
        verify(valueOperations).set("RT:" + TEST_USER_ID, REFRESH_TOKEN, 14, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 USER_NOT_FOUND 예외가 발생한다.")
    void login_Fail_UserNotFound() {
        // Given
        LoginRequest request = createLoginRequest("wrong@ssafy.com", TEST_PASSWORD);
        given(userRepository.findByEmail("wrong@ssafy.com")).willReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 불일치 시 INVALID_PASSWORD 예외가 발생한다.")
    void login_Fail_InvalidPassword() {
        // Given
        User user = createTestUser();
        LoginRequest request = createLoginRequest(TEST_EMAIL, "wrong-password");

        given(userRepository.findByEmail(TEST_EMAIL)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", ENCODED_PASSWORD)).willReturn(false);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    @Test
    @DisplayName("로그아웃 성공 시 Redis에서 Refresh Token을 삭제한다.")
    void logout_Success() {
        // Given
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(REFRESH_TOKEN);
        LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);

        // When
        authService.logout(request, TEST_USER_ID);

        // Then
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("로그아웃 시 Redis에 토큰이 없으면 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void logout_Fail_NoTokenInRedis() {
        // Given
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(null);
        LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> authService.logout(request, TEST_USER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("로그아웃 시 요청된 토큰과 Redis의 토큰이 다르면 INVALID_REFRESH_TOKEN 예외가 발생한다.")
    void logout_Fail_TokenMismatch() {
        // Given
        String redisKey = "RT:" + TEST_USER_ID;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(redisKey)).willReturn(REFRESH_TOKEN);
        LogoutRequest request = new LogoutRequest("mismatch-token");

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> authService.logout(request, TEST_USER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // --- Helper Methods ---

    private User createTestUser() {
        User user = User.builder()
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .name(TEST_NAME)
                .status(UserStatus.ACTIVE)
                .build();
        // ReflectionTestUtils를 사용하여 private 필드인 userId 설정
        ReflectionTestUtils.setField(user, "userId", TEST_USER_ID);
        return user;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "loginId", email);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }
}
