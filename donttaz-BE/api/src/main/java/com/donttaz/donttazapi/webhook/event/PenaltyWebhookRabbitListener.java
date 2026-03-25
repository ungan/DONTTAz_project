package com.donttaz.donttazapi.webhook.event;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import com.donttaz.donttazapi.webhook.sender.WebhookDispatcher;
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
    private String fixedWebhookUrl;

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

        // 상세 정보 조회 (사용자 닉네임, 금고 명, 인출 횟수 등)
        WishlistVault vault = wishlistVaultRepository
                .findById(event.wishlistVaultId())
                .orElse(null);
        String nickname = "알 수 없는 사용자";
        String wishName = "알 수 없는 금고";
        int withdrawalCount = 0;

        if (vault != null) {
            wishName = vault.getWishName();
            withdrawalCount = vault.getWithdrawalCount();
            nickname = userRepository.findById(vault.getUserId())
                    .map(User::getNickname)
                    .orElse("알 수 없는 사용자");
        }

        String statusHeader = event.terminated() ? "금고 해지 (규칙 위반)" : "부분 출금";

        // 상세 마크다운 템플릿 생성
        String detailedMessage = String.format(
                "### [%s님 수신] 🚨 위시리스트 규칙 위반 알림\n" +
                        "📌 **대상 위시리스트**: %s\n" +
                        "💰 **이번 출금 금액**: %,d원\n" +
                        "🔢 **현재 인출 횟수**: %d회\n" +
                        "⚠️ **상태**: %s\n\n" +
                        "💬 **수치심 메시지**:\n" +
                        "> %s",
                nickname,
                wishName,
                event.withdrawAmount(),
                withdrawalCount,
                statusHeader,
                webhook.getShameMessage());

        // 고정 웹훅 URL로 발송
        webhookDispatcher.dispatch(
                webhook.getPlatformType(),
                fixedWebhookUrl,
                detailedMessage);
    }
}
