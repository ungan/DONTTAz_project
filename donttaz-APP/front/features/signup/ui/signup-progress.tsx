import React from 'react';
import { View } from 'react-native';
import type { SignupStep } from '../types/signup';
import { SIGNUP_STEP_ORDER } from '../types/signup';
import { styles } from './signup-progress.styles';

interface SignupProgressProps {
  currentStep: SignupStep;
}

export function SignupProgress({ currentStep }: SignupProgressProps) {
  const currentIndex = SIGNUP_STEP_ORDER.indexOf(currentStep);
  const fillPercent = ((currentIndex + 1) / SIGNUP_STEP_ORDER.length) * 100;

  return (
    <View style={styles.track}>
      <View style={[styles.fill, { width: `${fillPercent}%` }]} />
    </View>
  );
}
