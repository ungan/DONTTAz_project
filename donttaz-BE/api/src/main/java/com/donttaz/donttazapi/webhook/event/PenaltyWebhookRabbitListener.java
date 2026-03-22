package com.donttaz.donttazapi.webhook.event;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import com.donttaz.donttazapi.webhook.sender.WebhookDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PenaltyWebhookRabbitListener {

    private final PenaltyWebhookRepository penaltyWebhookRepository;
    private final WebhookDispatcher webhookDispatcher;

    /**
     * 외부 메시지 큐(RabbitMQ)에서 이벤트를 구독하여 카카오톡 알림을 비동기로 안전하게 전송합니다.
     * 컨슈머 서버가 별도로 분리되더라도 동작하며, 메인 트랜잭션 에러 및 타임아웃 지연으로부터 완전히 독립됩니다.
     */
    @RabbitListener(queues = RabbitMQConfig.PENALTY_QUEUE)
    public void receivePenaltyEvent(VaultPenaltyEvent event) {
        log.info("[RabbitMQ] 🐰 금고 패널티 이벤트(수치심 메시지) 큐 수신 - WishlistVaultId: {}", event.wishlistVaultId());

        penaltyWebhookRepository.findByWishlistVaultId(event.wishlistVaultId())
                .ifPresentOrElse(
                        webhook -> {
                            log.info("[RabbitMQ] 수치심 메시지 설정을 찾았습니다. 웹훅을 발송합니다.");
                            webhookDispatcher.dispatch(
                                    webhook.getPlatformType(),
                                    webhook.getWebhookToken(),
                                    webhook.getShameMessage());
                        },
                        () -> log.info("[RabbitMQ] 위시리스트에 등록된 수치심(웹훅) 설정이 없어 스킵합니다."));
    }
}
