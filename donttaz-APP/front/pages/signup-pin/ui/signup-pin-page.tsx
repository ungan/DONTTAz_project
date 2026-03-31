import React, { useEffect } from 'react';
import { View } from 'react-native';
import { router } from 'expo-router';
import { PageHeader } from '@/shared/ui';
import {
  SignupPinPanel,
  SignupProgress,
  useSignupFlow,
} from '@/features/signup';
import { styles } from './signup-pin-page.styles';

export function SignupPinPage() {
  const {
    currentStep,
    isVerified,
    loginId,
    password,
    pinCode,
    pinCodeConfirm,
    pinError,
    handlePinKeyPress,
    submitPinStep,
  } = useSignupFlow();

  useEffect(() => {
    if (!isVerified) {
      router.replace('/signup-start' as never);
      return;
    }

    if (!loginId || !password) {
      router.replace('/signup-account' as never);
    }
  }, [isVerified, loginId, password]);

  useEffect(() => {
    if (pinCodeConfirm.length === 6) {
      submitPinStep();
    }
  }, [pinCodeConfirm]);

  return (
    <View style={styles.container}>
      <View style={styles.headerBlock}>
        <PageHeader title="PIN설정" />
        <SignupProgress currentStep={currentStep} />
      </View>

      <SignupPinPanel
        pinCode={pinCode}
        pinCodeConfirm={pinCodeConfirm}
        pinError={pinError}
        onKeyPress={handlePinKeyPress}
      />
    </View>
  );
}
