import React from 'react';
import { View } from 'react-native';
import type { AccountOpenStep } from '../types/account-open';
import { styles } from './account-open-progress.styles';

const ACCOUNT_OPEN_STEP_ORDER: AccountOpenStep[] = ['terms', 'verify', 'profile', 'banking'];

export function AccountOpenProgress({ currentStep }: { currentStep: AccountOpenStep }) {
  const currentIndex = ACCOUNT_OPEN_STEP_ORDER.indexOf(currentStep);
  const fillPercent = ((currentIndex + 1) / ACCOUNT_OPEN_STEP_ORDER.length) * 100;

  return (
    <View style={styles.track}>
      <View style={[styles.fill, { width: `${fillPercent}%` }]} />
    </View>
  );
}
