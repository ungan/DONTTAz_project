import React, { useEffect, useRef } from 'react';
import { View, Text } from 'react-native';
import { useURL } from 'expo-linking';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import {
  ACCOUNT_OPEN_PORTONE_CALLBACK_PATH,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { matchesPortOneCallbackPath } from '@/features/signup';
import { styles } from './account-open-verify-page.styles';

export function AccountOpenVerifyPage() {
  const callbackUrl = useURL();
  const handledCallbackUrlRef = useRef<string | null>(null);
  const {
    currentUserName,
    currentUserPhoneNumber,
    errorMessage,
    identityVerificationError,
    isRequiredTermsAgreed,
    isCreatePending,
    isIdentityVerificationPending,
    moveToVerifyStep,
    verifiedOwnerName,
    verifiedOwnerPhoneNumber,
    completeVerificationFromCallbackUrl,
    submitVerifyStep,
  } = useAccountOpenFlow();
  const moveToVerifyStepRef = useRef(moveToVerifyStep);
  const completeVerificationFromCallbackUrlRef = useRef(completeVerificationFromCallbackUrl);

  moveToVerifyStepRef.current = moveToVerifyStep;
  completeVerificationFromCallbackUrlRef.current = completeVerificationFromCallbackUrl;

  useEffect(() => {
    if (!isRequiredTermsAgreed) {
      router.replace('/account-open-terms' as never);
      return;
    }

    moveToVerifyStepRef.current();
  }, [isRequiredTermsAgreed]);

  useEffect(() => {
    if (!callbackUrl || handledCallbackUrlRef.current === callbackUrl) {
      return;
    }

    if (!matchesPortOneCallbackPath(callbackUrl, ACCOUNT_OPEN_PORTONE_CALLBACK_PATH)) {
      return;
    }

    handledCallbackUrlRef.current = callbackUrl;
    void completeVerificationFromCallbackUrlRef.current(callbackUrl);
  }, [callbackUrl]);

  return (
    <View style={styles.container}>
      <PageHeader title="계좌정보 인증" onBack={() => router.replace('/account-open-terms' as never)} />

      <AccountOpenProgress currentStep="verify" />

      <View style={styles.heroSection}>
        <Text style={styles.title}>본인인증 재확인</Text>
        <Text style={styles.description}>
          계좌 개설 전, PortOne 본인인증 결과와 현재 로그인한 사용자 정보를 다시 한 번 비교합니다.
        </Text>
        <Text style={styles.caption}>
          이름과 휴대폰 번호가 모두 일치하면 개설 정보 입력 단계로 이동합니다.
        </Text>
      </View>

      <View style={styles.summaryCard}>
        <Text style={styles.summaryTitle}>비교 기준</Text>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>현재 사용자 이름</Text>
          <Text style={styles.summaryValue}>{currentUserName || '-'}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>현재 사용자 휴대폰</Text>
          <Text style={styles.summaryValue}>{currentUserPhoneNumber || '-'}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>최근 인증 이름</Text>
          <Text style={styles.summaryValue}>{verifiedOwnerName || '-'}</Text>
        </View>
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>최근 인증 휴대폰</Text>
          <Text style={styles.summaryValue}>{verifiedOwnerPhoneNumber || '-'}</Text>
        </View>
      </View>

      {identityVerificationError ? (
        <Text style={styles.errorText}>{identityVerificationError}</Text>
      ) : null}
      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      <Button
        label={
          isIdentityVerificationPending
            ? '본인인증 확인 중...'
            : isCreatePending
              ? '계좌 준비 중...'
              : '본인인증 다시 하기'
        }
        disabled={isIdentityVerificationPending || isCreatePending}
        isLoading={isIdentityVerificationPending || isCreatePending}
        onPress={submitVerifyStep}
      />

      <Text style={styles.helperText}>
        인증 완료 후 앱으로 자동 복귀가 실패하면, 인증 페이지의 앱 복귀 버튼을 눌러 다시 돌아오면 됩니다.
      </Text>
    </View>
  );
}
