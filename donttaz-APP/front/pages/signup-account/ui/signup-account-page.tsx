import React, { useEffect } from 'react';
import { ScrollView, View, Text, KeyboardAvoidingView } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Button, PageHeader } from '@/shared/ui';
import {
  SignupAccountForm,
  SignupProgress,
  SignupVerifiedSummary,
  useSignupFlow,
} from '@/features/signup';
import { styles } from './signup-account-page.styles';

export function SignupAccountPage() {
  const insets = useSafeAreaInsets();
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
    <View style={styles.outerContainer}>
      <KeyboardAvoidingView behavior="padding" style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.headerBlock}>
          <PageHeader title="계정설정" />
          <SignupProgress currentStep={currentStep} />
        </View>

        <View style={styles.section}>
          <Text style={styles.title}>이메일·비밀번호 설정</Text>
          <Text style={styles.description}>
            로그인에 사용할 이메일 주소와 비밀번호를 입력해 주세요.
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
      </ScrollView>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
        <Button label="다음" onPress={submitAccountStep} />
      </View>
      </KeyboardAvoidingView>
    </View>
  );
}
