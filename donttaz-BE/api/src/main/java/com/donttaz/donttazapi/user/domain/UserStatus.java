package com.donttaz.donttazapi.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    SUSPENDED("정지"),
    DELETED("탈퇴"),
    WITHDRAWN("회원탈퇴"); // 신규 추가

    private final String description;
}
