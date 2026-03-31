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
    private static final String WITHDRAWAL_NOTIFICATION_TITLE = "출금 알림";
    private static final String TRANSFER_NOTIFICATION_TITLE = "계좌이체 알림";
    private static final String PAYMENT_NOTIFICATION_TITLE = "결제 알림";

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
    public void sendDepositNotification(Long userId, Long amount, String summary) {
        sendNotification(userId, "DEPOSIT", DEPOSIT_NOTIFICATION_TITLE, buildDepositMessage(amount, summary), amount, summary);
    }

    @Override
    public void sendWithdrawalNotification(Long userId, Long amount, String summary) {
        sendNotification(userId, "WITHDRAWAL", WITHDRAWAL_NOTIFICATION_TITLE, buildWithdrawalMessage(amount, summary), amount, summary);
    }

    @Override
    public void sendTransferNotification(Long userId, Long amount, String summary) {
        sendNotification(userId, "TRANSFER", TRANSFER_NOTIFICATION_TITLE, buildTransferMessage(amount, summary), amount, summary);
    }

    @Override
    public void sendPaymentNotification(Long userId, Long amount, String merchantName) {
        sendNotification(userId, "PAYMENT", PAYMENT_NOTIFICATION_TITLE, buildPaymentMessage(amount, merchantName), amount, merchantName);
    }

    private void sendNotification(Long userId, String type, String title, String body, Long amount, String summary) {
        List<String> tokens = fcmTokenRepository.findAllByUserUserId(userId).stream()
                .map(FcmToken::getToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .toList();

        if (tokens.isEmpty()) {
            log.info("Skipping {} push notification because no FCM tokens were found. userId={}", type, userId);
            return;
        }

        if (firebaseMessaging.isEmpty()) {
            log.info(
                    "Skipping {} push notification because Firebase is not configured. userId={} tokenCount={}",
                    type,
                    userId,
                    tokens.size()
            );
            return;
        }

        log.info(
                "Sending {} push notification. userId={} amount={} tokenCount={} summary={}",
                type,
                userId,
                amount,
                tokens.size(),
                summary
        );

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
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
                .putData("type", type)
                .putData("amount", amount == null ? "0" : String.valueOf(amount))
                .putData("summary", summary == null ? "" : summary)
                .build();

        try {
            var response = firebaseMessaging.get().sendEachForMulticast(message);
            log.info(
                    "{} push notification completed. userId={} successCount={} failureCount={}",
                    type,
                    userId,
                    response.getSuccessCount(),
                    response.getFailureCount()
            );
            if (response.getFailureCount() > 0) {
                log.warn(
                        "{} push notification partially failed. userId={} successCount={} failureCount={}",
                        type,
                        userId,
                        response.getSuccessCount(),
                        response.getFailureCount()
                );
                logFailureDetails(type, tokens, response.getResponses(), userId);
            }
        } catch (FirebaseMessagingException exception) {
            log.warn(
                    "{} push notification failed. userId={} message={}",
                    type,
                    userId,
                    exception.getMessage()
            );
        }
    }

    private void logFailureDetails(String type, List<String> tokens, List<SendResponse> responses, Long userId) {
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse.isSuccessful()) {
                continue;
            }

            FirebaseMessagingException exception = sendResponse.getException();
            String maskedToken = i < tokens.size() ? maskToken(tokens.get(i)) : "unknown";
            log.warn(
                    "FCM delivery failed type={} userId={} token={} errorCode={} messagingErrorCode={} message={}",
                    type,
                    userId,
                    maskedToken,
                    exception == null ? null : exception.getErrorCode(),
                    exception == null ? null : exception.getMessagingErrorCode(),
                    exception == null ? null : exception.getMessage()
            );
        }
    }

    private String buildDepositMessage(Long amount, String summary) {
        return appendSummary(formatAmount(amount) + "이 입금됐어요.", summary);
    }

    private String buildWithdrawalMessage(Long amount, String summary) {
        return appendSummary(formatAmount(amount) + "이 출금됐어요.", summary);
    }

    private String buildTransferMessage(Long amount, String summary) {
        return appendSummary(formatAmount(amount) + "을 이체했어요.", summary);
    }

    private String buildPaymentMessage(Long amount, String merchantName) {
        String formattedAmount = formatAmount(amount);
        if (merchantName == null || merchantName.isBlank()) {
            return formattedAmount + "이 결제됐어요.";
        }

        return merchantName.trim() + "에서 " + formattedAmount + "이 결제됐어요.";
    }

    private String appendSummary(String message, String summary) {
        if (summary == null || summary.isBlank()) {
            return message;
        }

        return message + " (" + summary.trim() + ")";
    }

    private String formatAmount(Long amount) {
        return amount == null ? "0원" : String.format("%,d원", amount);
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
