import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import {
  SignupPinPanel,
  SignupProgress,
  SignupVerifiedSummary,
  useSignupFlow,
} from '@/features/signup';
import { styles } from './signup-pin-page.styles';

export function SignupPinPage() {
  const {
    currentStep,
    isVerified,
    verifiedName,
    verifiedPhoneNumber,
    loginId,
    password,
    pinCode,
    pinCodeConfirm,
    pinError,
    handlePinKeyPress,
    resetPinEntry,
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

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="PIN 설정" />

      <SignupProgress currentStep={currentStep} />

      <View style={styles.section}>
        <Text style={styles.title}>간편하게 다시 들어올 준비를 해요</Text>
        <Text style={styles.description}>
          PIN은 앱 재실행 후 간편 재인증에 사용할 예정입니다. 이번 MVP에서는 회원가입 시 함께
          설정만 해둡니다.
        </Text>
      </View>

      <SignupVerifiedSummary
        name={verifiedName}
        phoneNumber={verifiedPhoneNumber}
      />

      <SignupPinPanel
        pinCode={pinCode}
        pinCodeConfirm={pinCodeConfirm}
        pinError={pinError}
        onKeyPress={handlePinKeyPress}
      />

      <View style={styles.helperRow}>
        <Button
          label="다시 입력"
          variant="outline"
          onPress={resetPinEntry}
        />
        <Button
          label="회원가입 완료로 이동"
          onPress={submitPinStep}
        />
      </View>

      <Text style={styles.helperText}>
        같은 숫자 6자리를 두 번 입력하면 다음 단계로 이동합니다.
      </Text>
    </ScrollView>
  );
}
