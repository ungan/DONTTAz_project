package com.donttaz.donttazapi.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.service.AuthService;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    private static final String TEST_NAME = "김싸피";
    private static final Long TEST_USER_ID = 1L;

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

    @Test
    @DisplayName("로그인 성공 시 토큰을 발급하고 Redis에 저장한다.")
    void loginSuccess() {
        // Given
        String email = "test@ssafy.com";
        String rawPassword = "password123!";
        String encodedPassword = "encoded-password";
        User user = createUser(email, encodedPassword);
        LoginRequest request = createLoginRequest(email, rawPassword);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, encodedPassword)).willReturn(true);
        given(jwtProvider.createAccessToken(TEST_USER_ID, email)).willReturn("access-token");
        given(jwtProvider.createRefreshToken(TEST_USER_ID, email)).willReturn("refresh-token");
        given(valueOperations.get("RT:" + TEST_USER_ID)).willReturn("refresh-token");

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getName()).isEqualTo(TEST_NAME);
        verify(valueOperations).set("RT:" + TEST_USER_ID, "refresh-token", 14, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다.")
    void loginFailWhenUserNotFound() {
        // Given
        LoginRequest request = createLoginRequest("missing@ssafy.com", "password123!");
        given(userRepository.findByEmail("missing@ssafy.com")).willReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 예외가 발생한다.")
    void loginFailWhenPasswordMismatch() {
        // Given
        String email = "test@ssafy.com";
        String encodedPassword = "encoded-password";
        User user = createUser(email, encodedPassword);
        LoginRequest request = createLoginRequest(email, "wrong-password");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", encodedPassword)).willReturn(false);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> authService.login(request));

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
    }

    private User createUser(String email, String encodedPassword) {
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .pinCode("encoded-pin")
                .name(TEST_NAME)
                .phoneNumber("01012345678")
                .status(UserStatus.ACTIVE)
                .build();
        setField(user, "userId", TEST_USER_ID);
        return user;
    }

    private LoginRequest createLoginRequest(String loginId, String password) {
        LoginRequest request = new LoginRequest();
        setField(request, "loginId", loginId);
        setField(request, "password", password);
        return request;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
