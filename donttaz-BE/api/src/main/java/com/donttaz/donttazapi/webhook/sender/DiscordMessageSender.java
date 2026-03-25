package com.donttaz.donttazapi.webhook.sender;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordMessageSender implements WebhookSender {

    private final RestTemplate restTemplate;

    @Async
    @Override
    public void sendMessage(String webhookUrl, String message) {
        log.info("[Discord Webhook] 메시지 발송 시작 - URL: {}", webhookUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Discord 인커밍 웹훅 규격: {"content": "내용"}
        Map<String, String> body = Map.of("content", message);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("[Discord Webhook] 메시지 발송 완료");
        } catch (Exception e) {
            log.error("[Discord Webhook Error] 디스코드 발송 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(PlatformType platformType) {
        return platformType == PlatformType.DISCORD;
    }
}
