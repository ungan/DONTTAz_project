package com.donttaz.donttazapi.webhook.event;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.domain.PlatformType;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import com.donttaz.donttazapi.webhook.sender.WebhookDispatcher;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PenaltyWebhookRabbitListenerTest {

    @Mock
    private PenaltyWebhookRepository penaltyWebhookRepository;

    @Mock
    private WebhookDispatcher webhookDispatcher;

    @InjectMocks
    private PenaltyWebhookRabbitListener penaltyWebhookRabbitListener;

    @Test
    @DisplayName("금고 패널티 이벤트 수신 시 설정이 있으면 디스패처를 호출한다.")
    void receivePenaltyEvent_SendsWebhook() {
        // Given
        Long vaultId = 1L;
        VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId);

        PenaltyWebhook webhook = PenaltyWebhook.builder()
                .userId(1L)
                .wishlistVaultId(vaultId)
                .platformType(PlatformType.MATTERMOST)
                .shameMessage("야식 그만")
                .build();

        given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));

        // When
        penaltyWebhookRabbitListener.receivePenaltyEvent(event);

        // Then
        verify(webhookDispatcher).dispatch(PlatformType.MATTERMOST, "url", "message");
    }

    @Test
    @DisplayName("설정이 없으면 디스패처를 호출하지 않는다.")
    void receivePenaltyEvent_SkipsWhenNoConfig() {
        // Given
        VaultPenaltyEvent event = new VaultPenaltyEvent(2L);
        given(penaltyWebhookRepository.findByWishlistVaultId(anyLong())).willReturn(Optional.empty());

        // When
        penaltyWebhookRabbitListener.receivePenaltyEvent(event);

        // Then
        verifyNoInteractions(webhookDispatcher);
    }
}
