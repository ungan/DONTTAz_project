package com.donttaz.donttazapi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.DeviceType;
import com.donttaz.donttazapi.user.domain.FcmToken;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.dto.request.*;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.dto.response.UserUpdateResponse;
import com.donttaz.donttazapi.user.repository.FcmTokenRepository;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.user.service.UserCommandService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private static final Long TEST_USER_ID = 1L;

    @Mock private UserRepository userRepository;
    @Mock private FcmTokenRepository fcmTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BankingClient bankingClient;

    @InjectMocks private UserCommandService userCommandService;

    @Test
    @DisplayName("회원가입: 사용자 키를 발급받아 암호화된 사용자 정보를 저장한다.")
    void createUser_Success() {
        // [Given]
        String email = "test@ssafy.com";
        CreateUserRequest request = CreateUserRequest.builder()
                .email(email).password("pass123").pinCode("123456")
                .name("김싸피").phoneNumber("01012345678").deviceId("dev-1").build();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(bankingClient.createUserKey(email)).willReturn("ssafy-key");
        given(passwordEncoder.encode(anyString())).willReturn("encoded-val");
        given(userRepository.save(any(User.class))).willAnswer(inv -> {
            User u = inv.getArgument(0);
            setUserId(u);
            return u;
        });

        // [When]
        CreateUserResponse response = userCommandService.createUser(request);

        // [Then]
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("FCM 토큰 등록: 신규 기기인 경우 토큰을 새로 저장한다.")
    void registerFcmToken_NewDevice_Success() {
        // [Given]
        User user = createTestUser();
        FcmTokenRegisterRequest request = mock(FcmTokenRegisterRequest.class);
        given(request.getDeviceId()).willReturn("device-1");
        given(request.getFcmToken()).willReturn("token-aaa");
        given(request.getDeviceType()).willReturn(DeviceType.ANDROID);

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(fcmTokenRepository.findByDeviceId("device-1")).willReturn(Optional.empty());

        // [When]
        userCommandService.registerFcmToken(TEST_USER_ID, request);

        // [Then]
        verify(fcmTokenRepository).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("FCM 토큰 등록: 기존 기기인 경우 기존 토큰 정보를 업데이트한다.")
    void registerFcmToken_ExistingDevice_Success() {
        // [Given]
        User user = createTestUser();
        FcmTokenRegisterRequest request = mock(FcmTokenRegisterRequest.class);
        given(request.getDeviceId()).willReturn("device-1");
        given(request.getFcmToken()).willReturn("new-token");

        FcmToken existingToken = FcmToken.builder()
                .user(user).deviceId("device-1").token("old-token").deviceType(DeviceType.ANDROID).build();

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(fcmTokenRepository.findByDeviceId("device-1")).willReturn(Optional.of(existingToken));

        // [When]
        userCommandService.registerFcmToken(TEST_USER_ID, request);

        // [Then]
        assertThat(existingToken.getToken()).isEqualTo("new-token");
        verify(fcmTokenRepository, never()).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("회원 정보 수정: 닉네임을 변경하고 수정된 정보를 반환한다.")
    void updateUserInfo_Success() {
        // [Given]
        User user = createTestUser();
        UserUpdateRequest request = mock(UserUpdateRequest.class);
        given(request.getNickname()).willReturn("NewNick");
        
        ReflectionTestUtils.setField(user, "updatedAt", LocalDateTime.now());
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        // [When]
        UserUpdateResponse response = userCommandService.updateUserInfo(TEST_USER_ID, request);

        // [Then]
        assertThat(user.getNickname()).isEqualTo("NewNick");
        assertThat(response.getNickname()).isEqualTo("NewNick");
        assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("회원 탈퇴: 비밀번호가 일치하면 논리적 삭제 및 FCM 토큰 전체 삭제.")
    void withdraw_Success() {
        // [Given]
        User user = createTestUser();
        UserWithdrawRequest request = mock(UserWithdrawRequest.class);
        given(request.getPassword()).willReturn("correct-pass");
        given(request.getReason()).willReturn("bye");

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("correct-pass", user.getPassword())).willReturn(true);

        // [When]
        userCommandService.withdraw(TEST_USER_ID, request);

        // [Then]
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        verify(fcmTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("회원 탈퇴: 비밀번호 불일치 시 예외를 던진다.")
    void withdraw_Fail_InvalidPassword() {
        // [Given]
        User user = createTestUser();
        UserWithdrawRequest request = mock(UserWithdrawRequest.class);
        given(request.getPassword()).willReturn("wrong");

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong", user.getPassword())).willReturn(false);

        // [When & Then]
        assertThrows(CustomException.class, () -> userCommandService.withdraw(TEST_USER_ID, request));
        assertThat(user.getStatus()).isNotEqualTo(UserStatus.WITHDRAWN);
    }

    // --- Helpers ---

    private User createTestUser() {
        User user = User.builder()
                .email("test@ssafy.com").password("encoded-pass").name("김싸피").build();
        setUserId(user);
        return user;
    }

    private void setUserId(User user) {
        ReflectionTestUtils.setField(user, "userId", TEST_USER_ID);
    }
}
