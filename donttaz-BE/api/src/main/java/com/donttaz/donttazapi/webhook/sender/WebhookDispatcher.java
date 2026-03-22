package com.donttaz.donttazapi.webhook.sender;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final List<WebhookSender> senders;

    public void dispatch(PlatformType platformType, String token, String message) {
        WebhookSender activeSender = senders.stream()
                .filter(sender -> sender.supports(platformType))
                .findFirst()
                .orElse(null);

        if (activeSender != null) {
            log.info("[Webhook Dispatcher] 매칭된 Sender({})를 통해 비동기 전송을 시작합니다.", activeSender.getClass().getSimpleName());
            activeSender.sendMessage(token, message);
        } else {
            log.warn("[Webhook Dispatcher] {}에 대한 Sender 구현체를 찾을 수 없어 발송을 스킵합니다.", platformType);
        }
    }
}
