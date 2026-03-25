package com.donttaz.donttazapi.webhook.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlatformType {
    MATTERMOST("메터모스트");

    private final String description;
}
