package com.donttaz.donttazapi.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserWithdrawRequest {

    private String reason; // 탈퇴 사유 (Optional)

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String password;
}
