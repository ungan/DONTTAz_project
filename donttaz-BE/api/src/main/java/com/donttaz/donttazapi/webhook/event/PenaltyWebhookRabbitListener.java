package com.donttaz.donttazapi.webhook.event;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.webhook.domain.PenaltyWebhook;
import com.donttaz.donttazapi.webhook.repository.PenaltyWebhookRepository;
import com.donttaz.donttazapi.webhook.sender.KakaoAuthClient;
import com.donttaz.donttazapi.webhook.sender.WebhookDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PenaltyWebhookRabbitListener {

    private final PenaltyWebhookRepository penaltyWebhookRepository;
    private final WebhookDispatcher webhookDispatcher;
    private final KakaoAuthClient kakaoAuthClient;

    /**
     * 외부 메시지 큐(RabbitMQ)에서 이벤트를 구독하여 카카오톡 알림을 비동기로 안전하게 전송합니다.
     * 컨슈머 서버가 별도로 분리되더라도 동작하며, 메인 트랜잭션 에러 및 타임아웃 지연으로부터 완전히 독립됩니다.
     */
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.PENALTY_QUEUE)
    public void receivePenaltyEvent(VaultPenaltyEvent event) {
        log.info("[RabbitMQ] 🐰 금고 패널티 이벤트(수치심 메시지) 큐 수신 - WishlistVaultId: {}", event.wishlistVaultId());

        penaltyWebhookRepository.findByWishlistVaultId(event.wishlistVaultId())
                .ifPresentOrElse(
                        webhook -> {
                            // 토큰 만료 체크 및 리프레시 토큰을 활용한 자동 갱신 (Lazy-Refresh)
                            String activeToken = getOrRefreshToken(webhook);

                            log.info("[RabbitMQ] 수치심 메시지 설정을 찾았습니다. 웹훅을 발송합니다.");
                            webhookDispatcher.dispatch(
                                    webhook.getPlatformType(),
                                    activeToken,
                                    webhook.getReceiverUuuids(),
                                    webhook.getShameMessage());
                        },
                        () -> log.info("[RabbitMQ] 위시리스트에 등록된 수치심(웹훅) 설정이 없어 스킵합니다."));
    }

    private String getOrRefreshToken(PenaltyWebhook webhook) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 만료시각 정보가 있고 2. 만료가 10분 이내로 임박했으며 3. 리프레시 토큰이 존재하는 경우에만 시행
        if (webhook.getAccessTokenExpiresAt() != null &&
                webhook.getAccessTokenExpiresAt().isBefore(now.plusMinutes(10)) &&
                webhook.getRefreshToken() != null) {

            log.info("[RabbitMQ] 액세스 토큰 만료가 임박하여 카카오 인증 서버를 통해 자동 갱신을 시도합니다.");
            KakaoAuthClient.KakaoTokenResponse response = kakaoAuthClient.refreshToken(webhook.getRefreshToken());

            if (response != null) {
                webhook.updateAccessToken(response.accessToken(), now.plusSeconds(response.expiresIn()));
                // 카카오 정책상 리프레시 토큰이 새로 내려올 수도 있으므로 업데이트 로직 포함 가능
                // 현재는 기존 리프레시 토큰을 유지하는 로직으로 일단 수행
                penaltyWebhookRepository.save(webhook);
                return response.accessToken();
            }
        }
        return webhook.getWebhookToken();
    }
}
