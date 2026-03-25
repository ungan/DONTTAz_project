package com.donttaz.donttazapi.user.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.DeviceType;
import com.donttaz.donttazapi.user.domain.FcmToken;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.request.FcmTokenRegisterRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.repository.FcmTokenRepository;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@ssafy.com";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ENCODED_PIN = "encoded-pin";
    private static final String SSAFY_USER_KEY = "ssafy-user-key";

    @Mock
    private UserRepository userRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BankingClient bankingClient;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("createUser returns saved user info")
    void createUserSuccess() {
        CreateUserRequest request = createCreateUserRequest(TEST_EMAIL);
        User savedUser = createSavedUser(TEST_EMAIL, TEST_USER_ID);

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(bankingClient.createUserKey(TEST_EMAIL)).willReturn(SSAFY_USER_KEY);
        given(passwordEncoder.encode(request.getPassword())).willReturn(ENCODED_PASSWORD);
        given(passwordEncoder.encode(request.getPinCode())).willReturn(ENCODED_PIN);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        CreateUserResponse response = userCommandService.createUser(request);

        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getName()).isEqualTo(request.getName());

        verify(userRepository).save(any(User.class));
        verify(bankingClient).createUserKey(TEST_EMAIL);
    }

    @Test
    @DisplayName("createUser throws when email already exists")
    void createUserFailEmailDuplicated() {
        CreateUserRequest request = createCreateUserRequest("duplicate@ssafy.com");
        given(userRepository.existsByEmail("duplicate@ssafy.com")).willReturn(true);

        assertThrows(CustomException.class, () -> userCommandService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, bankingClient);
    }

    @Test
    @DisplayName("registerFcmToken saves a new token for a new device")
    void registerFcmTokenSavesNewTokenWhenDeviceIsNew() {
        User user = createSavedUser(TEST_EMAIL, TEST_USER_ID);
        FcmTokenRegisterRequest request = createFcmTokenRegisterRequest("new-token", "device-1", DeviceType.ANDROID);
        ArgumentCaptor<FcmToken> tokenCaptor = ArgumentCaptor.forClass(FcmToken.class);

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));
        given(fcmTokenRepository.findByDeviceId("device-1")).willReturn(Optional.empty());

        userCommandService.registerFcmToken(TEST_USER_ID, request);

        verify(fcmTokenRepository).save(tokenCaptor.capture());
        FcmToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getToken()).isEqualTo("new-token");
        assertThat(savedToken.getDeviceId()).isEqualTo("device-1");
        assertThat(savedToken.getDeviceType()).isEqualTo(DeviceType.ANDROID);
    }

    @Test
    @DisplayName("registerFcmToken reassigns an existing device to the current user")
    void registerFcmTokenReassignsExistingDeviceToCurrentUser() {
        User previousUser = createSavedUser("prev@ssafy.com", 2L);
        User currentUser = createSavedUser(TEST_EMAIL, TEST_USER_ID);
        FcmToken existingToken = FcmToken.builder()
                .user(previousUser)
                .token("old-token")
                .deviceId("device-1")
                .deviceType(DeviceType.IOS)
                .build();
        FcmTokenRegisterRequest request = createFcmTokenRegisterRequest("new-token", "device-1", DeviceType.ANDROID);

        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(currentUser));
        given(fcmTokenRepository.findByDeviceId("device-1")).willReturn(Optional.of(existingToken));

        userCommandService.registerFcmToken(TEST_USER_ID, request);

        verify(fcmTokenRepository, never()).save(any(FcmToken.class));
        assertThat(existingToken.getUser()).isSameAs(currentUser);
        assertThat(existingToken.getToken()).isEqualTo("new-token");
        assertThat(existingToken.getDeviceType()).isEqualTo(DeviceType.ANDROID);
    }

    private CreateUserRequest createCreateUserRequest(String email) {
        return CreateUserRequest.builder()
                .email(email)
                .password("password123!")
                .pinCode("123456")
                .name("Test User")
                .phoneNumber("01012345678")
                .deviceId("device-id")
                .build();
    }

    private User createSavedUser(String email, Long userId) {
        User user = User.builder()
                .email(email)
                .password(ENCODED_PASSWORD)
                .pinCode(ENCODED_PIN)
                .name("Test User")
                .phoneNumber("01012345678")
                .ssafyUserKey(SSAFY_USER_KEY)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    private FcmTokenRegisterRequest createFcmTokenRegisterRequest(String token, String deviceId, DeviceType deviceType) {
        FcmTokenRegisterRequest request = new FcmTokenRegisterRequest();
        ReflectionTestUtils.setField(request, "fcmToken", token);
        ReflectionTestUtils.setField(request, "deviceId", deviceId);
        ReflectionTestUtils.setField(request, "deviceType", deviceType);
        return request;
    }
}
