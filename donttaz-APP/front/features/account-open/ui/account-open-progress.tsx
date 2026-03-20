import React from 'react';
import { View, Text } from 'react-native';
import type { AccountOpenStep } from '../types/account-open';
import { styles } from './account-open-progress.styles';

const STEPS: Array<{ key: AccountOpenStep; label: string }> = [
  { key: 'terms', label: '계좌개설' },
  { key: 'verify', label: '본인인증' },
  { key: 'profile', label: '정보입력' },
  { key: 'banking', label: '전자금융' },
];

export function AccountOpenProgress({ currentStep }: { currentStep: AccountOpenStep }) {
  return (
    <View style={styles.container}>
      {STEPS.map((step) => {
        const isActive = step.key === currentStep;

        return (
          <View key={step.key} style={styles.stepItem}>
            <Text style={[styles.stepLabel, isActive && styles.stepLabelActive]}>{step.label}</Text>
            <View style={[styles.stepBar, isActive && styles.stepBarActive]} />
          </View>
        );
      })}
    </View>
  );
}
