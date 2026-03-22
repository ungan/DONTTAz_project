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

    // 카카오톡 "친구에게 보내기" 기본 호스트 (기존 '나에게 보내기'에서 변경)
    private static final String KAKAO_FRIENDS_API_URL = "https://kapi.kakao.com/v1/api/talk/friends/message/default/send";

    @Async
    @Override
    public void sendMessage(String token, String receiverUuuids, String message) {
        try {
            // 친구 전용 API는 수신자 UUID 목록이 필수입니다.
            if (receiverUuuids == null || receiverUuuids.isEmpty()) {
                log.warn("[KakaoTalk Webhook] 수신자(receiver_uuids) 정보가 없어 발송을 스킵합니다. (친구에게 보내기 모드)");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(token);

            // DB의 콤마 구분자 문자열("uuid1,uuid2")을 카카오가 원하는 JSON 배열 문자열("[\"uuid1\",\"uuid2\"]")로 변환
            String jsonUuuids = "[\"" + receiverUuuids.replace(",", "\",\"") + "\"]";

            // 피드(Feed) 템플릿 사용
            String templateObject = String.format(
                    "{\"object_type\":\"feed\",\"content\":{\"title\":\"[위시리스트 긴급 알림]\",\"description\":\"%s\",\"image_url\":\"https://donttaz.s3.ap-northeast-2.amazonaws.com/shame.png\",\"link\":{\"web_url\":\"https://donttaz.com\"}},\"buttons\":[{\"title\":\"정신차리러 가기\",\"link\":{\"web_url\":\"https://donttaz.com\"}}]}",
                    message.replace("\"", "\\\"") // JSON Escape
            );

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("receiver_uuids", jsonUuuids);
            body.add("template_object", templateObject);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(KAKAO_FRIENDS_API_URL, request, String.class);
            log.info("[KakaoTalk Webhook] 친구에게 수치심 메시지 발송 완료. Recipients count: {}", 
                    receiverUuuids.split(",").length);

        } catch (RestClientException e) {
            log.error("[KakaoTalk Webhook Error] 카카오톡 친구 메시지 발송 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(PlatformType platformType) {
        return platformType == PlatformType.KAKAOTALK;
    }
}
