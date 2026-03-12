package com.donttaz.donttazapi.common.security;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return parseUserId(user.getUsername());
        }

        if (principal instanceof String username && !username.isBlank() && !"anonymousUser".equals(username)) {
            return parseUserId(username);
        }

        throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
    }

    private Long parseUserId(String username) {
        try {
            return Long.parseLong(username);
        } catch (NumberFormatException exception) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }
}
