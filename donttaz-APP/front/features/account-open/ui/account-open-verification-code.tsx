import React from 'react';
import { View } from 'react-native';
import { styles } from './account-open-verification-code.styles';

export function AccountOpenVerificationCode({ filledCount }: { filledCount: number }) {
  return (
    <View style={styles.container}>
      {Array.from({ length: 4 }).map((_, index) => (
        <View key={index} style={[styles.box, index < filledCount && styles.boxFilled]} />
      ))}
    </View>
  );
}
