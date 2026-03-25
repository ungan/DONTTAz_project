package com.donttaz.donttazapi.webhook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.domain.PlatformType;
import com.donttaz.donttazapi.webhook.dto.request.PenaltyWebhookRequest;
import com.donttaz.donttazapi.webhook.dto.response.PenaltyWebhookResponse;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PenaltyWebhookServiceTest {

    @Mock
    private PenaltyWebhookRepository penaltyWebhookRepository;

    @Mock
    private WishlistVaultRepository wishlistVaultRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private PenaltyWebhookService penaltyWebhookService;

    @Test
    @DisplayName("신규 웹훅 설정은 무사히 저장되고 Response 객체를 반환한다.")
    void saveOrUpdateWebhook_NewSaveSuccess() {
        // Given
        Long userId = 1L;
        Long vaultId = 10L;
        given(currentUserProvider.getCurrentUserId()).willReturn(userId);

        WishlistVault vault = WishlistVault.builder().userId(userId).wishName("맥북").build();
        ReflectionTestUtils.setField(vault, "id", vaultId);
        given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));

        given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.empty());

        PenaltyWebhook savedWebhook = PenaltyWebhook.builder()
                .userId(userId)
                .wishlistVaultId(vaultId)
                .platformType(PlatformType.MATTERMOST)
                .shameMessage("야식 그만")
                .build();
        ReflectionTestUtils.setField(savedWebhook, "id", 100L);
        given(penaltyWebhookRepository.save(any(PenaltyWebhook.class))).willReturn(savedWebhook);

        PenaltyWebhookRequest request = new PenaltyWebhookRequest(PlatformType.MATTERMOST, "야식 그만");

        // When
        PenaltyWebhookResponse response = penaltyWebhookService.saveOrUpdateWebhook(vaultId, request);

        // Then
        assertThat(response.webhookId()).isEqualTo(100L);
        assertThat(response.shameMessage()).isEqualTo("야식 그만");
        verify(penaltyWebhookRepository).save(any(PenaltyWebhook.class));
    }

    @Test
    @DisplayName("다른 사람의 금고에 웹훅을 설정할 수 없다.")
    void saveOrUpdateWebhook_AccessDenied() {
        // Given
        Long myId = 1L;
        Long otherId = 2L;
        Long vaultId = 10L;
        given(currentUserProvider.getCurrentUserId()).willReturn(myId);

        WishlistVault otherVault = WishlistVault.builder().userId(otherId).wishName("맥북").build();
        ReflectionTestUtils.setField(otherVault, "id", vaultId);
        given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(otherVault));

        PenaltyWebhookRequest request = new PenaltyWebhookRequest(PlatformType.MATTERMOST, "야식 그만");

        // When & Then
        assertThrows(CustomException.class, () -> penaltyWebhookService.saveOrUpdateWebhook(vaultId, request));
    }
}
