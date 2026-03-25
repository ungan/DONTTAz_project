package com.donttaz.donttazapi.user.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.FcmToken;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.request.FcmTokenRegisterRequest;
import com.donttaz.donttazapi.user.dto.request.UserUpdateRequest;
import com.donttaz.donttazapi.user.dto.request.UserWithdrawRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.dto.response.UserUpdateResponse;
import com.donttaz.donttazapi.user.repository.FcmTokenRepository;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankingClient bankingClient;

    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String ssafyUserKey = bankingClient.createUserKey(request.getEmail());

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .pinCode(passwordEncoder.encode(request.getPinCode()))
            .name(request.getName())
            .phoneNumber(request.getPhoneNumber())
            .deviceId(request.getDeviceId())
            .ssafyUserKey(ssafyUserKey)
            .status(UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);

        return CreateUserResponse.builder()
            .userId(savedUser.getUserId())
            .email(savedUser.getEmail())
            .name(savedUser.getName())
            .build();
    }

    public void registerFcmToken(Long userId, FcmTokenRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        fcmTokenRepository.findByDeviceId(request.getDeviceId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateRegistration(user, request.getFcmToken(), request.getDeviceType()),
                        () -> fcmTokenRepository.save(FcmToken.builder()
                                .user(user)
                                .token(request.getFcmToken())
                                .deviceId(request.getDeviceId())
                                .deviceType(request.getDeviceType())
                                .build())
                );
    }

    public UserUpdateResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.updateNickname(request.getNickname());

        return UserUpdateResponse.builder()
                .nickname(user.getNickname())
                .updatedAt(user.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    public void withdraw(Long userId, UserWithdrawRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.withdraw(request.getReason());
        fcmTokenRepository.deleteByUser(user);
    }
}
