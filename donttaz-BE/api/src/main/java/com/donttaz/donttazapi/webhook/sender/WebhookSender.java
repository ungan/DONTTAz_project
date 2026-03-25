package com.donttaz.donttazapi.webhook.sender;

import com.donttaz.donttazapi.webhook.domain.PlatformType;

public interface WebhookSender {
    /**
     * 지정된 토큰(URL)과 플랫폼을 기반으로 알림 메시지를 외부로 전송합니다.
     * 
     * @param token          대상 식별 토큰 또는 웹훅 URL
     * @param receiverUuuids 메시지를 수신할 대상 목록 (카카오톡 친구 등)
     * @param message        전송할 사용자 커스텀 메시지 문구
     */
    void sendMessage(String token, String message);

    /**
     * 해당 Sender가 특정 플랫폼(Kakao, Mattermost 등)을 지원하는지 확인합니다.
     */
    boolean supports(PlatformType platformType);
}
