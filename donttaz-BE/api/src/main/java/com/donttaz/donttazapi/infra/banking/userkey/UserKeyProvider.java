package com.donttaz.donttazapi.infra.banking.userkey;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserKeyProvider {

    private final UserRepository userRepository;

    public UserKeyProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUserKey(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String userKey = user.getSsafyUserKey();
        if (userKey == null || userKey.isBlank()) {
            throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED, "userKey is not available.");
        }

        return userKey;
    }
}
