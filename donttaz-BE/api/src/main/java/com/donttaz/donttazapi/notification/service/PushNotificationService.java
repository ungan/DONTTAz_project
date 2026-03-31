package com.donttaz.donttazapi.notification.service;

public interface PushNotificationService {

    void sendDepositNotification(Long userId, Long amount, String summary);

    void sendWithdrawalNotification(Long userId, Long amount, String summary);

    void sendTransferNotification(Long userId, Long amount, String summary);

    void sendPaymentNotification(Long userId, Long amount, String merchantName);
}
