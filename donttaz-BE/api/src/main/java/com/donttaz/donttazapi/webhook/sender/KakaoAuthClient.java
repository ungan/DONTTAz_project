package com.donttaz.donttazapi.webhook.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
public class KakaoAuthClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret:}")
    private String clientSecret;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";

    public KakaoAuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
     * 카카오 정책상 리프레시 토큰도 함께 갱신되어 내려올 수 있습니다.
     */
    public KakaoTokenResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("refresh_token", refreshToken);

        // Client Secret은 설정되어 있을 때만 전송 (선택 사항)
        if (clientSecret != null && !clientSecret.isEmpty()) {
            params.add("client_secret", clientSecret);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> body = response.getBody();

            if (body != null) {
                log.info("[Kakao Auth] 카카오 토큰 갱신 요청 성공");
                return new KakaoTokenResponse(
                        (String) body.get("access_token"),
                        (String) body.get("refresh_token"),
                        ((Number) body.get("expires_in")).longValue());
            }
        } catch (Exception e) {
            log.error("[Kakao Auth] 카카오 토큰 갱신 실패: {}", e.getMessage());
        }
        return null;
    }

    public record KakaoTokenResponse(String accessToken, String refreshToken, long expiresIn) {
    }
}
