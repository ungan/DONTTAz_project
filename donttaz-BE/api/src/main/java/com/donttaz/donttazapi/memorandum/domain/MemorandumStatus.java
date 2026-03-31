package com.donttaz.donttazapi.memorandum.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemorandumStatus {
    WRITING("작성 중"),
    SIGNED("서명 완료"),
    EXPOSED_WITHDRAW("일부 인출로 인한 노출"),
    EXPOSED_TERMINATE("중도 해지로 인한 노출");

    private final String description;
}
