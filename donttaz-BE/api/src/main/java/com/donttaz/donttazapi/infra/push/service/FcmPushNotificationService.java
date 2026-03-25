package com.donttaz.donttazapi.infra.push.service;

import com.donttaz.donttazapi.notification.service.PushNotificationService;
import com.donttaz.donttazapi.user.domain.FcmToken;
import com.donttaz.donttazapi.user.repository.FcmTokenRepository;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmPushNotificationService implements PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FcmPushNotificationService.class);
    private static final String DEFAULT_ANDROID_CHANNEL_ID = "default";
    private static final String DEPOSIT_NOTIFICATION_TITLE = "입금 알림";

    private final FcmTokenRepository fcmTokenRepository;
    private final Optional<FirebaseMessaging> firebaseMessaging;

    public FcmPushNotificationService(
            FcmTokenRepository fcmTokenRepository,
            Optional<FirebaseMessaging> firebaseMessaging
    ) {
        this.fcmTokenRepository = fcmTokenRepository;
        this.firebaseMessaging = firebaseMessaging;
    }

    @Override
    public void sendDepositNotification(Long userId, String accountNo, Long amount, String summary) {
        List<String> tokens = fcmTokenRepository.findAllByUserUserId(userId).stream()
                .map(FcmToken::getToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();
        String maskedAccountNo = maskAccountNumber(accountNo);
        String notificationBody = buildDepositMessage(accountNo, amount, summary);

        if (tokens.isEmpty()) {
            log.info(
                    "입금 푸시를 건너뜁니다. 등록된 FCM 토큰이 없습니다. userId={} accountNo={}",
                    userId,
                    maskedAccountNo
            );
            return;
        }

        if (firebaseMessaging.isEmpty()) {
            log.info(
                    "입금 푸시를 건너뜁니다. Firebase 설정이 없습니다. userId={} accountNo={} tokenCount={}",
                    userId,
                    maskedAccountNo,
                    tokens.size()
            );
            return;
        }

        log.info(
                "입금 푸시 발송을 시도합니다. userId={} accountNo={} amount={} tokenCount={} summary={}",
                userId,
                maskedAccountNo,
                amount,
                tokens.size(),
                summary
        );

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(DEPOSIT_NOTIFICATION_TITLE)
                        .setBody(notificationBody)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(AndroidNotification.builder()
                                .setChannelId(DEFAULT_ANDROID_CHANNEL_ID)
                                .setPriority(AndroidNotification.Priority.MAX)
                                .setDefaultSound(true)
                                .setDefaultVibrateTimings(true)
                                .build())
                        .build())
                .putData("type", "DEPOSIT")
                .putData("accountNo", maskedAccountNo)
                .putData("amount", amount == null ? "0" : String.valueOf(amount))
                .build();

        try {
            var response = firebaseMessaging.get().sendEachForMulticast(message);
            log.info(
                    "입금 푸시 발송이 완료됐습니다. userId={} accountNo={} successCount={} failureCount={}",
                    userId,
                    maskedAccountNo,
                    response.getSuccessCount(),
                    response.getFailureCount()
            );
            if (response.getFailureCount() > 0) {
                log.warn(
                        "입금 푸시 발송 중 일부가 실패했습니다. userId={} accountNo={} successCount={} failureCount={}",
                        userId,
                        maskedAccountNo,
                        response.getSuccessCount(),
                        response.getFailureCount()
                );
                logFailureDetails(tokens, response.getResponses(), userId, maskedAccountNo);
            }
        } catch (FirebaseMessagingException exception) {
            log.warn(
                    "입금 푸시 발송에 실패했습니다. userId={} accountNo={} message={}",
                    userId,
                    maskedAccountNo,
                    exception.getMessage()
            );
        }
    }

    private void logFailureDetails(
            List<String> tokens,
            List<SendResponse> responses,
            Long userId,
            String maskedAccountNo
    ) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = sendResponse.getException();
            String maskedToken = i < tokens.size() ? maskToken(tokens.get(i)) : "unknown";
            log.warn(
                    "FCM delivery failed userId={} accountNo={} token={} errorCode={} messagingErrorCode={} message={}",
                    userId,
                    maskedAccountNo,
                    maskedToken,
                    exception == null ? null : exception.getErrorCode(),
                    exception == null ? null : exception.getMessagingErrorCode(),
                    exception == null ? null : exception.getMessage()
            );
        }
    }

    private String buildDepositMessage(String accountNo, Long amount, String summary) {
        String formattedAmount = amount == null ? "0원" : String.format("%,d원", amount);
        String maskedAccountNo = maskAccountNumber(accountNo);

        if (summary == null || summary.isBlank()) {
            return maskedAccountNo + " 계좌로 " + formattedAmount + "이 입금됐어요.";
        }

        return summary + " - " + maskedAccountNo + " 계좌로 " + formattedAmount + "이 입금됐어요.";
    }

    private String maskAccountNumber(String accountNo) {
        if (accountNo == null || accountNo.length() < 6) {
            return accountNo == null ? "" : accountNo;
        }

        return accountNo.substring(0, 3) + "-***-" + accountNo.substring(accountNo.length() - 6);
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }

        if (token.length() <= 12) {
            return token.substring(0, Math.min(4, token.length())) + "***";
        }

        return token.substring(0, 6) + "***" + token.substring(token.length() - 6);
    }
}