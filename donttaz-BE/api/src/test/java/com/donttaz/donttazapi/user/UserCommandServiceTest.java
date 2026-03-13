package com.donttaz.donttazapi.user;

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
import com.donttaz.donttazapi.user.service.UserCommandService;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private static final Long TEST_USER_ID = 1L;

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
    void createUserSuccess() {
        // Given
        String email = "test@ssafy.com";
        CreateUserRequest request = CreateUserRequest.builder()
                .email(email)
                .password("password123!")
                .pinCode("123456")
                .name("김싸피")
                .phoneNumber("01012345678")
                .deviceId("device-unique-id")
                .build();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(bankingClient.createUserKey(email)).willReturn("ssafy-user-key");
        given(passwordEncoder.encode("password123!")).willReturn("encoded-password");
        given(passwordEncoder.encode("123456")).willReturn("encoded-pin");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User user = invocation.getArgument(0);
            setUserId(user);
            return user;
        });

        // When
        CreateUserResponse response = userCommandService.createUser(request);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getPinCode()).isEqualTo("encoded-pin");
        assertThat(savedUser.getName()).isEqualTo("김싸피");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(savedUser.getDeviceId()).isEqualTo("device-unique-id");
        assertThat(savedUser.getSsafyUserKey()).isEqualTo("ssafy-user-key");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("김싸피");
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 회원가입에 실패한다.")
    void createUserFailWhenEmailDuplicated() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("duplicate@ssafy.com")
                .password("password123!")
                .pinCode("123456")
                .name("김싸피")
                .phoneNumber("01012345678")
                .build();

        given(userRepository.existsByEmail("duplicate@ssafy.com")).willReturn(true);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> userCommandService.createUser(request));

        // Then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, bankingClient);
    }

    private void setUserId(User user) {
        try {
            Field field = User.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(user, TEST_USER_ID);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
