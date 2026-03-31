package com.donttaz.donttazapi.webhook.sender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WebhookDispatcherTest {

    @Test
    @DisplayName("플랫폼 타입에 맞는 Sender를 찾아 메시지 발송을 위임한다.")
    void dispatch_DelegatesToSupportedSender() {
        // Given
        WebhookSender mattermostSender = mock(WebhookSender.class);
        when(mattermostSender.supports(PlatformType.MATTERMOST)).thenReturn(true);

        WebhookDispatcher dispatcher = new WebhookDispatcher(List.of(mattermostSender));

        String token = "webhook-url";
        String message = "hello";

        // When
        dispatcher.dispatch(PlatformType.MATTERMOST, token, message);

        // Then
        verify(mattermostSender).sendMessage(token, message);
    }
}
