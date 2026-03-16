import React from 'react';
import { View, Text } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './welcoming-alert.styles';

interface WelcomingAlertProps {
  userName?: string;
}

export function WelcomingAlert({ userName = '사용자' }: WelcomingAlertProps) {
  return (
    <View style={styles.container}>
      <View style={styles.textGroup}>
        <Text style={styles.greeting}>Good morning!</Text>
        <Text style={styles.userName}>{userName}</Text>
      </View>

      <View style={styles.notificationBox}>
        <Ionicons name="notifications-outline" size={16} color="#FFFFFF" />
      </View>
    </View>
  );
}
