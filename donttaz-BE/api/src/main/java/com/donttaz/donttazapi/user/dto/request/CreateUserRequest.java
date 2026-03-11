package com.donttaz.donttazapi.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateUserRequest {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 유효하지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_=+~]).{8,16}$",
        message = "비밀번호는 8~16자 영문, 숫자, 특수문자(!@#$%^&*()_=+~)를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "간편 비밀번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "간편 비밀번호는 숫자 6자리여야 합니다.")
    private String pinCode;

    @NotBlank(message = "실명은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "휴대폰 번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "휴대폰 번호는 숫자만 입력 가능합니다.")
    private String phoneNumber;

    private String deviceId;

    @Builder
    public CreateUserRequest(String email, String password, String pinCode, String name, String phoneNumber, String deviceId) {
        this.email = email;
        this.password = password;
        this.pinCode = pinCode;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.deviceId = deviceId;
    }
}
