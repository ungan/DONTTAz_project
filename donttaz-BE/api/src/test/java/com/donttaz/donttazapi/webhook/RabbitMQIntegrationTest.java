package com.donttaz.donttazapi.webhook;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("local")
class RabbitMQIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("RabbitMQ 페널티 이벤트 발행 테스트 (Exchange/Queue 연동 확인)")
    void testPenaltyEventPublishing() {
        // Given
        VaultPenaltyEvent event = new VaultPenaltyEvent(99999L); // 가벨 ID

        // When & Then
        // 시각적 확인을 위해 100번 반복 발행합니다.
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PENALTY_EXCHANGE,
                        RabbitMQConfig.PENALTY_ROUTING_KEY,
                        new VaultPenaltyEvent((long) i));
            }
        });

        System.out.println(">>> RabbitMQ 페널티 이벤트 발행 성공!");
    }
}
