package com.donttaz.donttazapi.webhook.service;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.dto.request.PenaltyWebhookRequest;
import com.donttaz.donttazapi.webhook.dto.response.PenaltyWebhookResponse;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PenaltyWebhookService {

    private final PenaltyWebhookRepository penaltyWebhookRepository;
    private final WishlistVaultRepository wishlistVaultRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public PenaltyWebhookResponse saveOrUpdateWebhook(Long wishlistVaultId, PenaltyWebhookRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();

        WishlistVault vault = wishlistVaultRepository.findById(wishlistVaultId)
                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

        if (!vault.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.VAULT_ACCESS_DENIED);
        }

        LocalDateTime expiresAt = request.expiresIn() != null
                ? LocalDateTime.now().plusSeconds(request.expiresIn())
                : null;

        String uuidsString = request.receiverUuuids() != null
                ? String.join(",", request.receiverUuuids())
                : null;

        PenaltyWebhook webhook = penaltyWebhookRepository.findByWishlistVaultId(wishlistVaultId)
                .orElseGet(() -> PenaltyWebhook.builder()
                        .userId(userId)
                        .wishlistVaultId(wishlistVaultId)
                        .platformType(request.platformType())
                        .webhookToken(request.webhookToken())
                        .refreshToken(request.refreshToken())
                        .accessTokenExpiresAt(expiresAt)
                        .receiverUuuids(uuidsString)
                        .shameMessage(request.shameMessage())
                        .build());

        // Update if it already exists
        if (webhook.getId() != null) {
            webhook.updateWebhookSettings(
                    request.platformType(),
                    request.webhookToken(),
                    request.refreshToken(),
                    expiresAt,
                    uuidsString,
                    request.shameMessage());
        }

        PenaltyWebhook saved = penaltyWebhookRepository.save(webhook);
        return PenaltyWebhookResponse.from(saved);
    }

    public PenaltyWebhookResponse getWebhookByVaultId(Long wishlistVaultId) {
        Long userId = currentUserProvider.getCurrentUserId();

        PenaltyWebhook webhook = penaltyWebhookRepository.findByWishlistVaultId(wishlistVaultId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "웹훅 설정이 존재하지 않습니다."));

        if (!webhook.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.VAULT_ACCESS_DENIED);
        }

        return PenaltyWebhookResponse.from(webhook);
    }
}
