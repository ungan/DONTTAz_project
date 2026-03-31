package com.donttaz.donttazapi.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserUpdateResponse {
    private String nickname;
    private String updatedAt; // ISO 8601 포맷
}
