package com.donttaz.donttazapi.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private BankingClient bankingClient;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    @DisplayName("회원가입 성공 시 사용자 키를 발급받아 암호화된 사용자 정보를 저장한다.")
    void createUser_Success() {
        // [Given]
        CreateUserRequest request = createCreateUserRequest(TEST_EMAIL);
        User savedUser = createSavedUser(TEST_EMAIL);

        given(userRepository.existsByEmail(TEST_EMAIL)).willReturn(false);
        given(bankingClient.createUserKey(TEST_EMAIL)).willReturn(SSAFY_USER_KEY);
        given(passwordEncoder.encode(request.getPassword())).willReturn(ENCODED_PASSWORD);
        given(passwordEncoder.encode(request.getPinCode())).willReturn(ENCODED_PIN);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // [When]
        CreateUserResponse response = userCommandService.createUser(request);

        // [Then]
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getName()).isEqualTo(request.getName());

        verify(userRepository).save(any(User.class));
        verify(bankingClient).createUserKey(TEST_EMAIL);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 DUPLICATE_EMAIL 예외가 발생하며 저장을 시도하지 않는다.")
    void createUser_Fail_EmailDuplicated() {
        // [Given]
        CreateUserRequest request = createCreateUserRequest("duplicate@ssafy.com");
        given(userRepository.existsByEmail("duplicate@ssafy.com")).willReturn(true);

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            userCommandService.createUser(request);
        });
        
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, bankingClient);
    }

    // --- Helper Methods ---

    private CreateUserRequest createCreateUserRequest(String email) {
        return CreateUserRequest.builder()
                .email(email)
                .password("password123!")
                .pinCode("123456")
                .name("김싸피")
                .phoneNumber("01012345678")
                .deviceId("device-id")
                .build();
    }

    private User createSavedUser(String email) {
        User user = User.builder()
                .email(email)
                .password(ENCODED_PASSWORD)
                .pinCode(ENCODED_PIN)
                .name("김싸피")
                .phoneNumber("01012345678")
                .ssafyUserKey(SSAFY_USER_KEY)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(user, "userId", TEST_USER_ID);
        return user;
    }
}
