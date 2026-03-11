package com.donttaz.donttazapi.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private BankingClient bankingClient;

    @Test
    @DisplayName("회원가입 성공 - SSAFY User Key 발급 및 정보 저장")
    void signUpSuccess() throws Exception {
        // given
        String email = "test1224@ssafy.com";
        String rawPassword = "password123!";
        String rawPinCode = "123456";
        String issuedUserKey = "ssafy-test-user-key-789";

        CreateUserRequest request = CreateUserRequest.builder()
                .email(email)
                .password(rawPassword)
                .pinCode(rawPinCode)
                .name("김싸피")
                .phoneNumber("01012345678")
                .deviceId("device-unique-id")
                .build();

        given(bankingClient.createUserKey(anyString())).willReturn(issuedUserKey);

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value(email));

        // DB 검증
        User savedUser = userRepository.findByEmail(email).orElseThrow();
        assertThat(savedUser.getSsafyUserKey()).isEqualTo(issuedUserKey);
        assertThat(passwordEncoder.matches(rawPassword, savedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(rawPinCode, savedUser.getPinCode())).isTrue();
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 이메일")
    void signUpFailDuplicateEmail() throws Exception {
        // given
        String email = "duplicate@ssafy.com";
        userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("old-pw123!"))
                .pinCode(passwordEncoder.encode("111111"))
                .name("기존인")
                .phoneNumber("01000000000")
                .ssafyUserKey("old-key")
                .build());

        CreateUserRequest request = CreateUserRequest.builder()
                .email(email)
                .password("new-pw123!")
                .pinCode("222222")
                .name("신규인")
                .phoneNumber("01011112222")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("FAIL"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("회원가입 실패 - 유효하지 않은 비밀번호 형식")
    void signUpFailInvalidPassword() throws Exception {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@ssafy.com")
                .password("short") // 8자 미만
                .pinCode("123")    // 6자리 미만
                .name("에러")
                .phoneNumber("01012345678")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAIL"));
    }
}