package com.donttaz.donttazapi.webhook.event;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;

import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import com.donttaz.donttazapi.webhook.sender.WebhookDispatcher;
import com.donttaz.donttazapi.webhook.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PenaltyWebhookRabbitListener {

        private final PenaltyWebhookRepository penaltyWebhookRepository;
        private final WebhookDispatcher webhookDispatcher;
        private final UserRepository userRepository;
        private final WishlistVaultRepository wishlistVaultRepository;

        @Value("${webhook.mattermost.url}")
        private String mattermostUrl;

        @Value("${webhook.discord.url:}")
        private String discordUrl;

        /**
         * 외부 메시지 큐(RabbitMQ)에서 이벤트를 구독하여 수치심 메시지 알림을 비동기로 전송합니다.
         */
        @Transactional
        @RabbitListener(queues = RabbitMQConfig.PENALTY_QUEUE)
        public void receivePenaltyEvent(VaultPenaltyEvent event) {
                log.info("Received penalty event for vault ID: {}", event.wishlistVaultId());

                PenaltyWebhook webhook = penaltyWebhookRepository.findByWishlistVaultId(event.wishlistVaultId())
                                .orElse(null);

                if (webhook == null) {
                        log.warn("No webhook settings found for vault ID: {}", event.wishlistVaultId());
                        return;
                }

                WishlistVault vault = wishlistVaultRepository.findById(event.wishlistVaultId())
                                .orElse(null);

                if (vault == null) {
                        log.warn("WishlistVault not found for ID: {}", event.wishlistVaultId());
                        return;
                }

                String userName = userRepository.findById(vault.getUserId())
                                .map(com.donttaz.donttazapi.user.domain.User::getName)
                                .orElse(null);

                if (userName == null) {
                        log.warn("User not found for vault ID: {}", event.wishlistVaultId());
                        return;
                }

                String wishName = vault.getWishName();
                int withdrawalCount = vault.getWithdrawalCount();

                String detailedMessage;
                if (event.success()) {
                        detailedMessage = String.format(
                                        "# 🎉 축하합니다! ***목표 달성 완료!*** 🎉\n" +
                                                        "## 👏 %s님이 그 어려운 걸 해내셨습니다! (와... 진짜 안 꺼내실 줄은...)\n\n" +
                                                        "\"약속을 지킨 당신, 진정한 의지의 한국인입니다!\"\n\n" +
                                                        "🥳 **달성한 꿈**: %s\n" +
                                                        "💰 **최종 수령 금액**: %,d원\n" +
                                                        "⚠️ **최종 상태**: %s",
                                        userName,
                                        wishName,
                                        event.withdrawAmount(),
                                        "목표 달성 (Maturity)");
                } else {
                        if (event.terminated()) {
                                detailedMessage = String.format(
                                                "# ❗ 【 ***의지 완전 파산*** 】 ❗\n" +
                                                                "# ━━━━━━━━━━━━━━━━━━━━━━\n" +
                                                                "# \" ***%s*** \" \n" +
                                                                "# ━━━━━━━━━━━━━━━━━━━━━━\n" +
                                                                "### 👆 [끝남] 이렇게 호언장담하시더니... 결국 포기하셨군요, %s님? 🏳️\n\n" +
                                                                "\"와... 진짜로 금고를 깨버릴 줄은 몰랐네요. 미래의 내가 원망하지 않을까요? (의지력 0%%)\"\n\n" +
                                                                "💀 **파괴된 꿈**: %s\n" +
                                                                "🔥 **피 같은 내 돈(이었던 것)**: %,d원",
                                                webhook.getShameMessage(),
                                                userName,
                                                wishName,
                                                event.withdrawAmount());
                        } else {
                                detailedMessage = String.format(
                                                "# 🚨 %s님의 '의지박약' 주의보 발령!! 🚨\n" +
                                                                "# ━━━━━━━━━━━━━━━━━━━━━━\n" +
                                                                "# \" ***%s*** \" \n" +
                                                                "# ━━━━━━━━━━━━━━━━━━━━━━\n" +
                                                                "### 👆 [경고] 이렇게 호기롭게 선포하셨던 %s님의 의지가 흔들리고 있습니다! 🤨\n\n"
                                                                +
                                                                "\"에이, 딱 한 번만? 그 한 번이 두 번 되고 세 번 되는 거 아시죠? (정신 똑바로 차리세요!)\"\n\n"
                                                                +
                                                                "⚠️ **멀어진 꿈**: %s\n" +
                                                                "📉 **탈탈 털린 금액**: %,d원\n" +
                                                                "🔢 **출금 누적 횟수**: %d회",
                                                userName,
                                                webhook.getShameMessage(),
                                                userName,
                                                wishName,
                                                event.withdrawAmount(),
                                                withdrawalCount);
                        }
                }

                // 플랫폼에 따라 URL 선택 (개별 설정된 URL이 있으면 우선 사용, 없으면 전역 설정 사용)
                String targetUrl = webhook.getWebhookUrl();
                if (targetUrl == null || targetUrl.isBlank()) {
                        targetUrl = webhook.getPlatformType() == PlatformType.DISCORD ? discordUrl : mattermostUrl;
                }

                if (targetUrl == null || targetUrl.isBlank()) {
                        log.warn("Webhook URL for platform {} is not configured", webhook.getPlatformType());
                        return;
                }

                // 선택된 웹훅 URL로 발송
                webhookDispatcher.dispatch(
                                webhook.getPlatformType(),
                                targetUrl,
                                detailedMessage);
        }
}
