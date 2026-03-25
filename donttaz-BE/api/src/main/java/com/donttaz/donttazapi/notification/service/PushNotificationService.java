package com.donttaz.donttazapi.notification.service;

public interface PushNotificationService {

    void sendDepositNotification(Long userId, String accountNo, Long amount, String summary);
}
