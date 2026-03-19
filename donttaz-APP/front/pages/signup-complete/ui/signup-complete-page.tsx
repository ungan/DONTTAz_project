import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import { SignupProgress, useSignupFlow } from '@/features/signup';
import { styles } from './signup-complete-page.styles';

export function SignupCompletePage() {
  const {
    currentStep,
    isSigningUp,
    isAutoLoggingIn,
    errorMessage,
    isVerified,
    loginId,
    pinCode,
    pinCodeConfirm,
    submitSignupAndAutoLogin,
  } = useSignupFlow();

  useEffect(() => {
    if (!isVerified) {
      router.replace('/signup-start' as never);
      return;
    }

    if (!loginId) {
      router.replace('/signup-account' as never);
      return;
    }

    if (pinCode.length !== 6 || pinCodeConfirm.length !== 6) {
      router.replace('/signup-pin' as never);
      return;
    }

    void submitSignupAndAutoLogin();
  }, [isVerified, loginId, pinCode, pinCodeConfirm]);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="가입 완료" />

      <SignupProgress currentStep={currentStep} />

      <View style={styles.section}>
        <Text style={styles.title}>거의 다 끝났어요</Text>
        <Text style={styles.description}>
          회원가입을 완료하고 자동 로그인까지 이어서 처리하고 있습니다.
        </Text>
      </View>

      <View style={styles.statusCard}>
        <Text style={styles.statusText}>
          {isSigningUp
            ? '계정을 생성하고 있습니다.'
            : isAutoLoggingIn
              ? '자동 로그인 중입니다.'
              : errorMessage
                ? '처리 중 오류가 발생했습니다.'
                : '잠시만 기다려 주세요.'}
        </Text>
      </View>

      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      {errorMessage ? (
        <Button
          label="다시 시도"
          onPress={submitSignupAndAutoLogin}
        />
      ) : null}

      <Text style={styles.helperText}>
        자동 로그인이 끝나면 홈 화면으로 바로 이동합니다.
      </Text>
    </ScrollView>
  );
}
