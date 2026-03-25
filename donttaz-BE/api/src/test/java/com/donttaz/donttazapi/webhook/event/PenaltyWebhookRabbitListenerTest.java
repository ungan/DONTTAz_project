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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PenaltyWebhookRabbitListenerTest {

        @Mock
        private PenaltyWebhookRepository penaltyWebhookRepository;
        @Mock
        private WebhookDispatcher webhookDispatcher;
        @Mock
        private com.donttaz.donttazapi.user.repository.UserRepository userRepository;
        @Mock
        private com.donttaz.donttazapi.vault.repository.WishlistVaultRepository wishlistVaultRepository;

        private PenaltyWebhookRabbitListener penaltyWebhookRabbitListener;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                penaltyWebhookRabbitListener = new PenaltyWebhookRabbitListener(
                                penaltyWebhookRepository,
                                webhookDispatcher,
                                userRepository,
                                wishlistVaultRepository);
                org.springframework.test.util.ReflectionTestUtils.setField(penaltyWebhookRabbitListener,
                                "mattermostUrl",
                                "https://test-mattermost.com");
                org.springframework.test.util.ReflectionTestUtils.setField(penaltyWebhookRabbitListener, "discordUrl",
                                "https://test-discord.com");
        }

        @Test
        @DisplayName("디스코드 패널티 이벤트 수신 시 디스코드용 템플릿으로 전송한다.")
        void receivePenaltyEvent_SendsDiscordWebhook() {
                // Given
                Long vaultId = 1L;
                Long userId = 100L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 50000L, false);

                PenaltyWebhook webhook = PenaltyWebhook.builder()
                                .userId(userId)
                                .wishlistVaultId(vaultId)
                                .platformType(PlatformType.DISCORD)
                                .shameMessage("야식 금지")
                                .build();

                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder()
                                .userId(userId)
                                .wishName("맥북 사기")
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 1);

                com.donttaz.donttazapi.user.domain.User user = com.donttaz.donttazapi.user.domain.User.builder()
                                .nickname("홍길동")
                                .build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.of(user));

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.DISCORD),
                                org.mockito.ArgumentMatchers.eq("https://test-discord.com"),
                                org.mockito.ArgumentMatchers.contains("야식 금지"));
        }

        @Test
        @DisplayName("금고 패널티 이벤트 수신 시 상세 마크다운 템플릿을 생성하여 전송한다.")
        void receivePenaltyEvent_SendsDetailedWebhook() {
                // Given
                Long vaultId = 1L;
                Long userId = 100L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 50000L, false);

                PenaltyWebhook webhook = PenaltyWebhook.builder()
                                .userId(userId)
                                .wishlistVaultId(vaultId)
                                .platformType(PlatformType.MATTERMOST)
                                .shameMessage("야식 그만")
                                .build();

                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder()
                                .userId(userId)
                                .wishName("맥북 사기")
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 1);

                com.donttaz.donttazapi.user.domain.User user = com.donttaz.donttazapi.user.domain.User.builder()
                                .nickname("홍길동")
                                .build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.of(user));

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.MATTERMOST),
                                org.mockito.ArgumentMatchers.eq("https://test-mattermost.com"),
                                org.mockito.ArgumentMatchers.contains("홍길동"));
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.MATTERMOST),
                                org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.contains("맥북 사기"));
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.MATTERMOST),
                                org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.contains("1회"));
        }

        @Test
        @DisplayName("설정이 없으면 디스패처를 호출하지 않는다.")
        void receivePenaltyEvent_SkipsWhenNoConfig() {
                // Given
                VaultPenaltyEvent event = new VaultPenaltyEvent(2L, 10000L, true);
                given(penaltyWebhookRepository.findByWishlistVaultId(anyLong())).willReturn(Optional.empty());

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verifyNoInteractions(webhookDispatcher);
        }
}
