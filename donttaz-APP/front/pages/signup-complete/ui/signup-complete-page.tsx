import React, { useEffect } from 'react';
import { ScrollView, View, Text, Image } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Button, PageHeader } from '@/shared/ui';
import { SignupProgress, useSignupFlow } from '@/features/signup';
import { styles } from './signup-complete-page.styles';

function formatPhoneNumber(phoneNumber: string) {
  if (phoneNumber.length !== 11) return phoneNumber;
  return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7)}`;
}

export function SignupCompletePage() {
  const insets = useSafeAreaInsets();
  const {
    currentStep,
    isSigningUp,
    isAutoLoggingIn,
    errorMessage,
    isVerified,
    hasCreatedAccount,
    loginId,
    verifiedName,
    verifiedPhoneNumber,
    pinCode,
    pinCodeConfirm,
    submitSignupAndAutoLogin,
  } = useSignupFlow();

  const isLoading = isSigningUp || isAutoLoggingIn;

  useEffect(() => {
    if (hasCreatedAccount) return;
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
    }
  }, [isVerified, loginId, pinCode, pinCodeConfirm, hasCreatedAccount]);

  return (
    <View style={styles.outerContainer}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.headerBlock}>
          <PageHeader title="완료" />
          <SignupProgress currentStep={currentStep} />
        </View>

        <View style={styles.heroSection}>
          <Image
            source={require('@/shared/assets/icons/checked.png')}
            style={{ width: 88, height: 88 }}
          />
          <Text style={styles.heroTitle}>회원가입이 완료되었습니다</Text>
        </View>

        <View style={styles.summaryList}>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>이름</Text>
            <Text style={styles.summaryValue}>{verifiedName}</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>휴대폰</Text>
            <Text style={styles.summaryValue}>{formatPhoneNumber(verifiedPhoneNumber)}</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>이메일</Text>
            <Text style={styles.summaryValue}>{loginId}</Text>
          </View>
        </View>
      </ScrollView>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
        <Button
          label="완료"
          onPress={submitSignupAndAutoLogin}
          isLoading={isLoading}
        />
      </View>
    </View>
  );
}
