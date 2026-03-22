package com.donttaz.donttazapi.webhook.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlatformType {
    KAKAOTALK("카카오톡");

    private final String description;
}
