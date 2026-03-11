package com.donttaz.donttazapi.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.service.AuthService;
import com.donttaz.donttazapi.auth.util.JwtProvider;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class AuthLogoutTest {

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

    private final Long userId = 1L;
    private final String refreshToken = "valid-refresh-token";

    @BeforeEach
    void setUp() {
        // RedisTemplate의 opsForValue()가 valueOperations를 반환하도록 설정
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("로그아웃 성공 - 토큰 일치")
    void logoutSuccess() {
        // given
        String redisKey = "RT:" + userId;
        given(valueOperations.get(redisKey)).willReturn(refreshToken);

        LogoutRequest request = new LogoutRequest(refreshToken);

        // when & then
        assertDoesNotThrow(() -> authService.logout(request, userId));
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    @DisplayName("로그아웃 실패 - Redis에 토큰 없음")
    void logoutFailNoTokenInRedis() {
        // given
        String redisKey = "RT:" + userId;
        given(valueOperations.get(redisKey)).willReturn(null);

        LogoutRequest request = new LogoutRequest(refreshToken);

        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> authService.logout(request, userId));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그아웃 실패 - 토큰 불일치")
    void logoutFailTokenMismatch() {
        // given
        String redisKey = "RT:" + userId;
        given(valueOperations.get(redisKey)).willReturn(refreshToken);

        LogoutRequest request = new LogoutRequest("different-token");

        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> authService.logout(request, userId));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }
}
