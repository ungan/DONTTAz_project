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
    resetFromStart,
    startVerification,
  } = useSignupFlow();

  useEffect(() => {
    resetFromStart();
  }, []);

  useEffect(() => {
    if (!callbackUrl || handledCallbackUrlRef.current === callbackUrl) {
      return;
    }

    if (!matchesPortOneCallbackPath(callbackUrl, PORTONE_CALLBACK_PATH)) {
      return;
    }

    handledCallbackUrlRef.current = callbackUrl;
    void completeVerificationFromCallbackUrl(callbackUrl);
  }, [callbackUrl, completeVerificationFromCallbackUrl]);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="회원가입" />

      <SignupProgress currentStep={currentStep} />

      <View style={styles.section}>
        <Text style={styles.title}>본인인증부터 시작할게요</Text>
        <Text style={styles.description}>
          이름과 휴대폰 번호는 인증 결과값을 기준으로 가져오고, 이후 단계에서 계정과
          PIN을 설정합니다.
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>오늘 구현 범위</Text>
        <Text style={styles.cardText}>
          현재는 백엔드에 있는 PortOne 테스트 페이지를 브라우저 인증 세션으로 열고,
          callback으로 identityVerificationId를 받아오는 방식으로 연결합니다.
        </Text>
        <Text style={styles.cardText}>
          실제 연동 시에는 SDK callback에서 받은 identityVerificationId로 인증 결과 조회 API를
          호출하도록 교체하면 됩니다.
        </Text>
      </View>

      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      <Button
        label="본인인증 시작"
        onPress={startVerification}
        isLoading={isStartingVerification}
      />

      <Text style={styles.helperText}>
        이미 가입된 사용자 응답이 오면 다음 단계로 이동하지 않고 현재 화면에서 안내합니다.
      </Text>
    </ScrollView>
  );
}
