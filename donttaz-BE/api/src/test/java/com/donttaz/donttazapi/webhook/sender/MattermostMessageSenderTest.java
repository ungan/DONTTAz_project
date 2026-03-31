package com.donttaz.donttazapi.webhook.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class MattermostMessageSenderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MattermostMessageSender mattermostMessageSender;

    @Test
    @DisplayName("Mattermost 웹훅 전송 시 RestTemplate의 postForEntity가 올바른 파라미터로 호출된다.")
    void sendMessage_Success() {
        // Given
        String webhookUrl = "https://mattermost.com/hooks/test-token";
        String message = "테스트 메시지";

        // When
        mattermostMessageSender.sendMessage(webhookUrl, message);

        // Then
        verify(restTemplate).postForEntity(eq(webhookUrl), any(HttpEntity.class), eq(String.class));
    }

    @Test
    @DisplayName("Mattermost 플랫폼 타입을 지원한다.")
    void supports_Mattermost() {
        // When & Then
        assertThat(mattermostMessageSender.supports(PlatformType.MATTERMOST)).isTrue();
    }
}
