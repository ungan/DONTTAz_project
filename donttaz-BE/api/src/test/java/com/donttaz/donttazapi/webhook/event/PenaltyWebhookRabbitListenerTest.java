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
        @DisplayName("목표 달성 완료 시 축하 템플릿으로 전송한다.")
        void receivePenaltyEvent_WhenSuccess_SendsCelebration() {
                // Given
                Long vaultId = 1L;
                Long userId = 100L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 1000000L, false, true);

                PenaltyWebhook webhook = PenaltyWebhook.builder()
                                .userId(userId)
                                .wishlistVaultId(vaultId)
                                .platformType(PlatformType.DISCORD)
                                .build();

                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder()
                                .userId(userId)
                                .wishName("아이패드")
                                .build();

                com.donttaz.donttazapi.user.domain.User user = com.donttaz.donttazapi.user.domain.User.builder()
                                .name("짱구")
                                .build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.of(user));

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.DISCORD),
                                org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.argThat(s -> s.contains("🎉") && s.contains("목표 달성 완료")
                                                && s.contains("아이패드") && s.contains("1,000,000원") && !s.contains("출금")));
        }

        @Test
        @DisplayName("완전 해지(파산) 시 의지 파산 템플릿으로 전송하며 출금 횟수는 제외한다.")
        void receivePenaltyEvent_WhenTerminated_SendsFailureWithoutCount() {
                // Given
                Long vaultId = 1L;
                Long userId = 1000L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 50000L, true, false);

                PenaltyWebhook webhook = PenaltyWebhook.builder()
                                .userId(userId)
                                .wishlistVaultId(vaultId)
                                .platformType(PlatformType.DISCORD)
                                .shameMessage("나는 의지 박약이다")
                                .build();

                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder()
                                .userId(userId)
                                .wishName("맥북")
                                .build();

                com.donttaz.donttazapi.user.domain.User user = com.donttaz.donttazapi.user.domain.User.builder()
                                .name("철수")
                                .build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.of(user));

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.DISCORD),
                                org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.argThat(s -> s.contains("💀") && s.contains("의지 완전 파산")
                                                && s.contains("피 같은 내 돈") && s.contains("50,000원") && !s.contains("출금 누적 횟수")));
        }

        @Test
        @DisplayName("부분 출금 시 주의보 템플릿으로 전송하며 출금 횟수를 포함한다.")
        void receivePenaltyEvent_WhenPartialWithdrawal_SendsWarningWithCount() {
                // Given
                Long vaultId = 1L;
                Long userId = 1000L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 20000L, false, false);

                PenaltyWebhook webhook = PenaltyWebhook.builder()
                                .userId(userId)
                                .wishlistVaultId(vaultId)
                                .platformType(PlatformType.MATTERMOST)
                                .shameMessage("야식 참자")
                                .build();

                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder()
                                .userId(userId)
                                .wishName("플스5")
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 2);

                com.donttaz.donttazapi.user.domain.User user = com.donttaz.donttazapi.user.domain.User.builder()
                                .name("훈이")
                                .build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.of(user));

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verify(webhookDispatcher).dispatch(
                                org.mockito.ArgumentMatchers.eq(PlatformType.MATTERMOST),
                                org.mockito.ArgumentMatchers.anyString(),
                                org.mockito.ArgumentMatchers.argThat(s -> s.contains("의지박약")
                                                && s.contains("멀어진 꿈") && s.contains("20,000") && s.contains("2회")));
        }

        @Test
        @DisplayName("설정이 없으면 디스패처를 호출하지 않는다.")
        void receivePenaltyEvent_SkipsWhenNoConfig() {
                // Given
                VaultPenaltyEvent event = new VaultPenaltyEvent(2L, 10000L, true, true);
                given(penaltyWebhookRepository.findByWishlistVaultId(anyLong())).willReturn(Optional.empty());

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verifyNoInteractions(webhookDispatcher);
        }

        @Test
        @DisplayName("금고 정보를 찾을 수 없으면 디스패처를 호출하지 않는다.")
        void receivePenaltyEvent_SkipsWhenNoVault() {
                // Given
                Long vaultId = 999L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 10000L, true, true);
                PenaltyWebhook webhook = PenaltyWebhook.builder().wishlistVaultId(vaultId).build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.empty());

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verifyNoInteractions(webhookDispatcher);
        }

        @Test
        @DisplayName("사용자 정보를 찾을 수 없으면 디스패처를 호출하지 않는다.")
        void receivePenaltyEvent_SkipsWhenNoUser() {
                // Given
                Long vaultId = 1L;
                Long userId = 100L;
                VaultPenaltyEvent event = new VaultPenaltyEvent(vaultId, 10000L, true, true);
                PenaltyWebhook webhook = PenaltyWebhook.builder().userId(userId).wishlistVaultId(vaultId).build();
                com.donttaz.donttazapi.vault.domain.WishlistVault vault = com.donttaz.donttazapi.vault.domain.WishlistVault
                                .builder().userId(userId).build();

                given(penaltyWebhookRepository.findByWishlistVaultId(vaultId)).willReturn(Optional.of(webhook));
                given(wishlistVaultRepository.findById(vaultId)).willReturn(Optional.of(vault));
                given(userRepository.findById(userId)).willReturn(Optional.empty());

                // When
                penaltyWebhookRabbitListener.receivePenaltyEvent(event);

                // Then
                verifyNoInteractions(webhookDispatcher);
        }
}
