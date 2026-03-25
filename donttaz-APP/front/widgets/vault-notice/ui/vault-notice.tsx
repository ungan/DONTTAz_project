import React from 'react';
import { View, Text } from 'react-native';
import { styles } from './vault-notice.styles';

interface VaultNoticeProps {
  baseInterestRate: number;
  penaltyInterestRate: number;
}

export function VaultNotice({ baseInterestRate, penaltyInterestRate }: VaultNoticeProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.emoji}>🐭</Text>
      <View style={styles.textContainer}>
        <Text style={styles.text}>
          현재 이자율 <Text style={styles.bold}>{baseInterestRate}%</Text> 적용 중이에요.
        </Text>
        <Text style={styles.text}>
          중도 출금 시, 패널티 이자율{' '}
          <Text style={styles.bold}>{penaltyInterestRate}%</Text>가 적용됩니다.
        </Text>
      </View>
    </View>
  );
}
