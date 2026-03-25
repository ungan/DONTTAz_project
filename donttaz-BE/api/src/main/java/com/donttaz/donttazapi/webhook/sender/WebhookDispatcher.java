package com.donttaz.donttazapi.webhook.sender;

import com.donttaz.donttazapi.webhook.domain.PlatformType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final List<WebhookSender> senders;

    public void dispatch(PlatformType platformType, String token, String message) {
        senders.stream()
                .filter(sender -> sender.supports(platformType))
                .forEach(sender -> sender.sendMessage(token, message));
    }
}
