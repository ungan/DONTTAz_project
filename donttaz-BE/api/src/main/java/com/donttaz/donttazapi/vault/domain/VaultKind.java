package com.donttaz.donttazapi.vault.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VaultKind {
    DEFAULT("기본 금고"),
    WISHLIST("목표 금고");

    private final String description;
}
