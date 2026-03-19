import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import {
  SignupAccountForm,
  SignupProgress,
  SignupVerifiedSummary,
  useSignupFlow,
} from '@/features/signup';
import { styles } from './signup-account-page.styles';

export function SignupAccountPage() {
  const {
    currentStep,
    errorMessage,
    isVerified,
    verifiedName,
    verifiedPhoneNumber,
    loginId,
    password,
    passwordConfirm,
    loginIdError,
    passwordError,
    passwordConfirmError,
    updateLoginId,
    updatePassword,
    updatePasswordConfirm,
    submitAccountStep,
  } = useSignupFlow();

  useEffect(() => {
    if (!isVerified) {
      router.replace('/signup-start' as never);
    }
  }, [isVerified]);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="계정 정보 설정" />

      <SignupProgress currentStep={currentStep} />

      <View style={styles.section}>
        <Text style={styles.title}>로그인에 사용할 계정을 정해 주세요</Text>
        <Text style={styles.description}>
          인증된 본인 정보는 수정하지 않고, 이메일과 비밀번호만 직접 입력합니다.
        </Text>
      </View>

      <SignupVerifiedSummary
        name={verifiedName}
        phoneNumber={verifiedPhoneNumber}
      />

      <SignupAccountForm
        loginId={loginId}
        password={password}
        passwordConfirm={passwordConfirm}
        loginIdError={loginIdError}
        passwordError={passwordError}
        passwordConfirmError={passwordConfirmError}
        onLoginIdChange={updateLoginId}
        onPasswordChange={updatePassword}
        onPasswordConfirmChange={updatePasswordConfirm}
      />

      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      <Button
        label="다음"
        onPress={submitAccountStep}
      />
    </ScrollView>
  );
}
