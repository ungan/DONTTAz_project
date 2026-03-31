import React from 'react';
import { View, Text } from 'react-native';
import { styles } from './welcoming-alert.styles';

interface WelcomingAlertProps {
  userName?: string;
}

export function WelcomingAlert({ userName = '사용자' }: WelcomingAlertProps) {
  return (
    <View style={styles.container}>
      <View style={styles.textGroup}>
        <Text style={styles.userName}>
          <Text style={styles.userNameBold}>{userName}</Text>님,
        </Text>
        <Text style={styles.subText}>오늘도 절약 중!</Text>
      </View>
    </View>
  );
}
