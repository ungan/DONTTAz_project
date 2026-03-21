package com.donttaz.donttazapi.memorandum.dto.response;

import com.donttaz.donttazapi.memorandum.domain.MemorandumStatus;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MemorandumResponse(
        Long id,
        Long userId,
        String userName,
        String userPhoneNumber,
        Long wishlistVaultId,
        String wishName,
        String content1,
        String content2,
        String content3,
        MemorandumStatus status,
        LocalDateTime signedAt) {
}
