package com.donttaz.donttazapi.webhook.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlatformType {
    MATTERMOST("메터모스트"),
    DISCORD("디스코드");

    private final String description;
}
