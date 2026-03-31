package com.donttaz.donttazapi.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.donttaz.donttazapi.auth.dto.PortOneIdentityResponse;
import com.donttaz.donttazapi.auth.dto.request.PasswordResetRequest;
import com.donttaz.donttazapi.auth.dto.request.PasswordVerifyIdentityRequest;
import com.donttaz.donttazapi.auth.dto.response.PasswordResetTokenResponse;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class AuthPasswordServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private RestTemplate restTemplate;

    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("비밀번호 찾기: 본인인증 성공 및 CI 일치 시 리셋 토큰을 반환한다.")
    void verifyIdentityForPasswordReset_Success() {
        // [Given]
        String loginId = "test@ssafy.com";
        String ci = "USER_CI_1234";
        User user = User.builder().userId(1L).email(loginId).ci(ci).build();
        PasswordVerifyIdentityRequest request = new PasswordVerifyIdentityRequest(loginId, "cert-123");

        given(userRepository.findByEmail(loginId)).willReturn(Optional.of(user));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // PortOne API 응답 시뮬레이션
        PortOneIdentityResponse mockResponse = mock(PortOneIdentityResponse.class);
        PortOneIdentityResponse.VerifiedCustomer mockCustomer = mock(PortOneIdentityResponse.VerifiedCustomer.class);
        given(mockResponse.getStatus()).willReturn("VERIFIED");
        given(mockResponse.getVerifiedCustomer()).willReturn(mockCustomer);
        given(mockCustomer.getCi()).willReturn(ci);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PortOneIdentityResponse.class)))
                .willReturn(ResponseEntity.of(Optional.of(mockResponse)));

        // [When]
        PasswordResetTokenResponse response = authService.verifyIdentityForPasswordReset(request);

        // [Then]
        assertThat(response.getResetToken()).isNotNull();
        verify(valueOperations).set(anyString(), eq("1"), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    @DisplayName("비밀번호 찾기: 인증한 사람의 CI가 유저 정보와 다르면 예외를 던진다.")
    void verifyIdentityForPasswordReset_CiMismatch_ThrowsException() {
        // [Given]
        String loginId = "test@ssafy.com";
        User user = User.builder().email(loginId).ci("USER_REAL_CI").build();
        PasswordVerifyIdentityRequest request = new PasswordVerifyIdentityRequest(loginId, "cert-123");

        given(userRepository.findByEmail(loginId)).willReturn(Optional.of(user));

        // PortOne에서는 다른 사람(타인)의 CI가 온 상황 시뮬레이션
        PortOneIdentityResponse mockResponse = mock(PortOneIdentityResponse.class);
        PortOneIdentityResponse.VerifiedCustomer mockCustomer = mock(PortOneIdentityResponse.VerifiedCustomer.class);
        given(mockResponse.getStatus()).willReturn("VERIFIED");
        given(mockResponse.getVerifiedCustomer()).willReturn(mockCustomer);
        given(mockCustomer.getCi()).willReturn("OTHER_PERSON_CI");

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PortOneIdentityResponse.class)))
                .willReturn(ResponseEntity.of(Optional.of(mockResponse)));

        // [When & Then]
        assertThrows(CustomException.class, () -> authService.verifyIdentityForPasswordReset(request));
    }

    @Test
    @DisplayName("비밀번호 재설정: 유효한 토큰으로 비밀번호 변경 시 DB 반영 및 토큰 삭제.")
    void resetPassword_Success() {
        // [Given]
        String token = "valid-token";
        String newPassword = "newPass123!";
        PasswordResetRequest request = new PasswordResetRequest(token, newPassword);
        User user = User.builder().userId(1L).password("old_pass").build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn("1");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode(newPassword)).willReturn("encrypted_pass");

        // [When]
        authService.resetPassword(request);

        // [Then]
        assertThat(user.getPassword()).isEqualTo("encrypted_pass");
        verify(redisTemplate).delete(anyString());
    }
}
