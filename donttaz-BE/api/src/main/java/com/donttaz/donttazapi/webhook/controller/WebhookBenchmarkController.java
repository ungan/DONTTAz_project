package com.donttaz.donttazapi.webhook.controller;

import com.donttaz.donttazapi.common.config.RabbitMQConfig;
import com.donttaz.donttazapi.webhook.dto.response.BenchmarkResponse;
import com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/benchmark/webhook")
@Tag(name = "웹훅 성능 벤치마크", description = "동기 vs 비동기(RabbitMQ) 성능 비교 테스트용 API")
public class WebhookBenchmarkController {

    private final RabbitTemplate rabbitTemplate;

    @Operation(summary = "동기식 웹훅 호출 (가상 지연)", description = "내부적으로 1초의 지연(Thread.sleep)을 발생시켜 느린 외부 API 호출을 시뮬레이션합니다.")
    @GetMapping("/sync")
    public BenchmarkResponse testSync() {
        long startTime = System.currentTimeMillis();
        
        try {
            // 느린 외부 웹훅 API 시뮬레이션
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        return BenchmarkResponse.builder()
                .mode("Sync (Blocking)")
                .elapsedTimeMs(endTime - startTime)
                .message("외부 API 호출 완료 (1초 지연 시뮬레이션)")
                .build();
    }

    @Operation(summary = "비동기식 웹훅 호출 (RabbitMQ)", description = "RabbitMQ에 이벤트를 발행하고 즉시 응답합니다. 실제 발송은 백그라운드에서 이루어집니다.")
    @GetMapping("/async")
    public BenchmarkResponse testAsync() {
        long startTime = System.currentTimeMillis();

        // RabbitMQ에 이벤트 발행 (실제 발송은 PenaltyWebhookRabbitListener에서 비동기 처리됨)
        // 테스트용 더미 데이터 전송
        rabbitTemplate.convertAndSend(RabbitMQConfig.PENALTY_EXCHANGE,
                RabbitMQConfig.PENALTY_ROUTING_KEY,
                new VaultPenaltyEvent(0L, 0L, false, false));

        long endTime = System.currentTimeMillis();
        return BenchmarkResponse.builder()
                .mode("Async (RabbitMQ)")
                .elapsedTimeMs(endTime - startTime)
                .message("RabbitMQ 메시지 발행 완료 (백그라운드 처리)")
                .build();
    }
}
