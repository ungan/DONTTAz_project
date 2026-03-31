package com.donttaz.donttazapi.user.dto.request;

import com.donttaz.donttazapi.user.domain.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRegisterRequest {

    @NotBlank(message = "푸시 토큰은 필수입니다.")
    private String fcmToken;

    @NotNull(message = "기기 타입은 필수입니다.")
    private DeviceType deviceType;

    @NotBlank(message = "기기 식별자는 필수입니다.")
    private String deviceId;
}
