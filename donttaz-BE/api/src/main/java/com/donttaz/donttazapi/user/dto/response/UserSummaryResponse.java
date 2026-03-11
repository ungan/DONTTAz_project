package com.donttaz.donttazapi.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSummaryResponse {
    private Long userId;
    private String email;
    private String name;
}
