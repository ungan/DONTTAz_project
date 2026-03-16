package com.donttaz.donttazapi.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.dto.response.UserSummaryResponse;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@ssafy.com";
    private static final String TEST_NAME = "김싸피";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    @DisplayName("존재하는 사용자 ID를 조회하면 사용자 요약 정보를 반환한다.")
    void getUserSummary_Success() {
        // [Given]
        User user = createTestUser();
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.of(user));

        // [When]
        UserSummaryResponse response = userQueryService.getUserSummary(TEST_USER_ID);

        // [Then]
        assertThat(response.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(response.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(response.getName()).isEqualTo(TEST_NAME);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID를 조회하면 USER_NOT_FOUND 예외가 발생한다.")
    void getUserSummary_Fail_UserNotFound() {
        // [Given]
        given(userRepository.findById(TEST_USER_ID)).willReturn(Optional.empty());

        // [When & Then]
        assertThrows(CustomException.class, () -> {
            userQueryService.getUserSummary(TEST_USER_ID);
        });
    }

    // --- Helper Methods ---

    private User createTestUser() {
        User user = User.builder()
                .email(TEST_EMAIL)
                .name(TEST_NAME)
                .build();
        ReflectionTestUtils.setField(user, "userId", TEST_USER_ID);
        return user;
    }
}
