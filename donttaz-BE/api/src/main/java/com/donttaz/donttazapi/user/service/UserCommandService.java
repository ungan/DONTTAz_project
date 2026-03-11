package com.donttaz.donttazapi.user.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.domain.UserStatus;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
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
}
