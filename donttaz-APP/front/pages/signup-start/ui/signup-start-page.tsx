import React, { useEffect, useRef } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { useURL } from 'expo-linking';
import { Button, PageHeader } from '@/shared/ui';
import {
  matchesPortOneCallbackPath,
  PORTONE_CALLBACK_PATH,
  SignupProgress,
  useSignupFlow,
} from '@/features/signup';
import { styles } from './signup-start-page.styles';

export function SignupStartPage() {
  const callbackUrl = useURL();
  const handledCallbackUrlRef = useRef<string | null>(null);
  const {
    currentStep,
    errorMessage,
    isStartingVerification,
    completeVerificationFromCallbackUrl,
    startVerification,
  } = useSignupFlow();
  const completeVerificationFromCallbackUrlRef = useRef(completeVerificationFromCallbackUrl);

  completeVerificationFromCallbackUrlRef.current = completeVerificationFromCallbackUrl;

  useEffect(() => {
    if (!callbackUrl || handledCallbackUrlRef.current === callbackUrl) {
      return;
    }

    if (!matchesPortOneCallbackPath(callbackUrl, PORTONE_CALLBACK_PATH)) {
      return;
    }

    handledCallbackUrlRef.current = callbackUrl;
    void completeVerificationFromCallbackUrlRef.current(callbackUrl);
  }, [callbackUrl]);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.headerBlock}>
        <PageHeader title="본인인증" />
        <SignupProgress currentStep={currentStep} />
      </View>

      <View style={styles.section}>
        <Text style={styles.title}>휴대폰 본인인증</Text>
        <Text style={styles.description}>
          금융 서비스 이용을 위해 휴대폰으로 본인 확인을 진행합니다.
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>인증 시 확인하는 정보</Text>
        <Text style={styles.cardItem}>• 이름</Text>
        <Text style={styles.cardItem}>• 생년월일</Text>
        <Text style={styles.cardItem}>• 휴대폰 번호</Text>
      </View>

      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      <Button
        label="휴대폰 본인인증"
        onPress={startVerification}
        isLoading={isStartingVerification}
      />
    </ScrollView>
  );
}
