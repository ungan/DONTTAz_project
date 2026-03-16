package com.donttaz.donttazapi.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.donttaz.donttazapi.auth.dto.PortOneIdentityResponse;
import com.donttaz.donttazapi.auth.dto.request.FindIdRequest;
import com.donttaz.donttazapi.auth.dto.response.FindIdResponse;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthFindIdServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // @Value 필드 수동 주입 (단위 테스트 환경)
        ReflectionTestUtils.setField(authService, "apiSecret", "test-secret");
    }

    @Test
    @DisplayName("아이디 찾기: 본인인증 성공 시 마스킹된 아이디와 가입일을 반환한다.")
    void findId_Success() {
        // [Given]
        String ci = "USER_CI_1234";
        String email = "donttaz@ssafy.com";
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 16, 10, 0);
        
        User user = User.builder().email(email).ci(ci).build();
        // JPA가 관리하는 createdAt 필드 강제 주입
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        
        FindIdRequest request = new FindIdRequest("cert-123");

        // PortOne API 응답 모킹
        PortOneIdentityResponse mockResponse = mock(PortOneIdentityResponse.class);
        PortOneIdentityResponse.VerifiedCustomer mockCustomer = mock(PortOneIdentityResponse.VerifiedCustomer.class);
        given(mockResponse.getStatus()).willReturn("VERIFIED");
        given(mockResponse.getVerifiedCustomer()).willReturn(mockCustomer);
        given(mockCustomer.getCi()).willReturn(ci);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PortOneIdentityResponse.class)))
                .willReturn(ResponseEntity.ok(mockResponse));
        
        given(userRepository.findByCi(ci)).willReturn(Optional.of(user));

        // [When]
        FindIdResponse response = authService.findId(request);

        // [Then]
        assertThat(response.getMaskedLoginId()).isEqualTo("do****az@ssafy.com");
        assertThat(response.getCreatedAt()).isEqualTo("2026-03-16");
    }

    @Test
    @DisplayName("아이디 찾기: 인증은 성공했으나 가입된 유저가 없으면 USER_NOT_FOUND 예외를 던진다.")
    void findId_UserNotFound_ThrowsException() {
        // [Given]
        String ci = "NEW_USER_CI";
        FindIdRequest request = new FindIdRequest("cert-123");

        PortOneIdentityResponse mockResponse = mock(PortOneIdentityResponse.class);
        PortOneIdentityResponse.VerifiedCustomer mockCustomer = mock(PortOneIdentityResponse.VerifiedCustomer.class);
        given(mockResponse.getStatus()).willReturn("VERIFIED");
        given(mockResponse.getVerifiedCustomer()).willReturn(mockCustomer);
        given(mockCustomer.getCi()).willReturn(ci);

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PortOneIdentityResponse.class)))
                .willReturn(ResponseEntity.ok(mockResponse));
        
        given(userRepository.findByCi(ci)).willReturn(Optional.empty());

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> authService.findId(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("아이디 찾기: 본인인증 자체가 실패(FAILED)하면 IDENTITY_VERIFICATION_FAILED 예외를 던진다.")
    void findId_VerificationFailed_ThrowsException() {
        // [Given]
        FindIdRequest request = new FindIdRequest("invalid-cert");

        PortOneIdentityResponse mockResponse = mock(PortOneIdentityResponse.class);
        given(mockResponse.getStatus()).willReturn("FAILED");

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(PortOneIdentityResponse.class)))
                .willReturn(ResponseEntity.ok(mockResponse));

        // [When & Then]
        CustomException exception = assertThrows(CustomException.class, () -> authService.findId(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IDENTITY_VERIFICATION_FAILED);
    }
}
