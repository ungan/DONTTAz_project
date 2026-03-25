package com.donttaz.donttazapi.webhook.event;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent;
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

        // 고정 웹훅 URL로 발송
        webhookDispatcher.dispatch(
                webhook.getPlatformType(),
                fixedWebhookUrl,
                webhook.getShameMessage());
    }
}
