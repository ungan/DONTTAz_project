package com.donttaz.donttazapi.webhook.sender;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTalkMessageSender implements WebhookSender {

    private final RestTemplate restTemplate;

    // 카카오톡 "나에게 보내기" 텍스트 템플릿 기본 호스트
    private static final String KAKAO_API_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    @Async
    @Override
    public void sendMessage(String token, String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(token);

            // 간단한 텍스트 템플릿 구조
            String templateObject = String.format(
                    "{\"object_type\":\"text\",\"text\":\"[위시리스트 긴급 알림]\\n%s\",\"link\":{\"web_url\":\"https://donttaz.com\",\"mobile_web_url\":\"https://donttaz.com\"}}",
                    message.replace("\"", "\\\"") // JSON Escape
            );

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("template_object", templateObject);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(KAKAO_API_URL, request, String.class);
            log.info("[KakaoTalk Webhook] 수치심 메시지 발송 완료. token prefix: {}",
                    token.substring(0, Math.min(5, token.length())));

        } catch (RestClientException e) {
            log.error("[KakaoTalk Webhook Error] 카카오톡 메시지 발송 실패: {}", e.getMessage(), e);
            // 비동기로 동작하므로 로직 내 전체 트랜잭션을 롤백시키지는 않고 로그만 남깁니다.
        }
    }

    @Override
    public boolean supports(PlatformType platformType) {
        return platformType == PlatformType.KAKAOTALK;
    }
}
