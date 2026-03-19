import React from 'react';
import { View, Text } from 'react-native';
import type { SignupStep } from '../types/signup';
import { SIGNUP_STEP_ORDER } from '../types/signup';
import { styles } from './signup-progress.styles';

const STEP_LABELS: Record<SignupStep, string> = {
  start: '본인인증',
  account: '계정설정',
  pin: 'PIN설정',
  complete: '완료',
};

interface SignupProgressProps {
  currentStep: SignupStep;
}

export function SignupProgress({ currentStep }: SignupProgressProps) {
  const currentIndex = SIGNUP_STEP_ORDER.indexOf(currentStep);

  return (
    <View style={styles.container}>
      <View style={styles.labels}>
        {SIGNUP_STEP_ORDER.map((step, index) => (
          <Text
            key={step}
            style={[styles.label, index <= currentIndex && styles.activeLabel]}
          >
            {STEP_LABELS[step]}
          </Text>
        ))}
      </View>

      <View style={styles.bars}>
        {SIGNUP_STEP_ORDER.map((step, index) => (
          <View
            key={step}
            style={[styles.bar, index <= currentIndex && styles.activeBar]}
          />
        ))}
      </View>
    </View>
  );
}
