import React, { useEffect, useRef } from 'react';
import { View, Text } from 'react-native';
import { useURL } from 'expo-linking';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { Button, PageHeader } from '@/shared/ui';
import {
  ACCOUNT_OPEN_PORTONE_CALLBACK_PATH,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { matchesPortOneCallbackPath } from '@/features/signup';
import { styles } from './account-open-verify-page.styles';

export function AccountOpenVerifyPage() {
  const insets = useSafeAreaInsets();
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
    proceedToProfileStep,
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

  const isVerified = !!verifiedOwnerName && !!verifiedOwnerPhoneNumber;
  const displayName = isVerified ? verifiedOwnerName : (currentUserName || '-');
  const displayPhone = isVerified ? verifiedOwnerPhoneNumber : (currentUserPhoneNumber || '-');

  return (
    <View style={styles.container}>
      <View style={styles.headerBlock}>
        <PageHeader title="본인인증" onBack={() => router.replace('/account-open-terms' as never)} />
        <AccountOpenProgress currentStep="verify" />
      </View>

      <View style={styles.heroSection}>
        <Text style={styles.title}>본인 확인</Text>
        <Text style={styles.description}>
          {isVerified
            ? '본인인증이 완료되었습니다. 계속 진행해 주세요.'
            : '계좌 개설을 위해 본인인증을 진행합니다.'}
        </Text>
      </View>

      <View style={styles.infoCard}>
        {isVerified && (
          <>
            <View style={styles.verifiedBadge}>
              <Ionicons name="checkmark-circle" size={15} color="#5899FD" />
              <Text style={styles.verifiedBadgeText}>인증 완료</Text>
            </View>
            <View style={styles.divider} />
          </>
        )}
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>이름</Text>
          <Text style={styles.summaryValue}>{displayName}</Text>
        </View>
        <View style={[styles.innerDivider, isVerified && styles.innerDividerVerified]} />
        <View style={styles.summaryRow}>
          <Text style={styles.summaryLabel}>휴대폰</Text>
          <Text style={styles.summaryValue}>{displayPhone}</Text>
        </View>
      </View>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        {identityVerificationError ? (
          <Text style={styles.errorText}>{identityVerificationError}</Text>
        ) : null}
        {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

        {isVerified ? (
          <Button label="다음" onPress={proceedToProfileStep} />
        ) : (
          <>
            <Button
              label={isIdentityVerificationPending ? '본인인증 확인 중...' : '본인인증 하기'}
              disabled={isIdentityVerificationPending || isCreatePending}
              isLoading={isIdentityVerificationPending || isCreatePending}
              onPress={submitVerifyStep}
            />
            <Text style={styles.helperText}>자동 복귀가 안 되면 복귀 버튼을 눌러 주세요.</Text>
          </>
        )}
      </View>
    </View>
  );
}
