import React, { useRef } from 'react';
import { useRouter } from 'expo-router';
import { IdentityVerification } from '@portone/react-native-sdk';
import { useAccountOpenFlow } from '@/features/account-open';
import { styles } from './account-open-certification-page.styles';

interface PortOneIdentityVerificationResponse {
  identityVerificationId?: string | null;
  code?: string | null;
  message?: string | null;
}

const PORTONE_STORE_ID = 'store-e9d6a7c5-fb1d-4094-a7b6-c2fe37b55e8a';
const PORTONE_CHANNEL_KEY = 'channel-key-f0acd432-a0ca-4c76-a481-1a219e2fc9a5';

function createIdentityVerificationRequest() {
  return {
    storeId: PORTONE_STORE_ID,
    identityVerificationId: `identity-${Date.now()}`,
    channelKey: PORTONE_CHANNEL_KEY,
  };
}

export function AccountOpenCertificationPage() {
  const router = useRouter();
  const requestRef = useRef(createIdentityVerificationRequest());
  const { completeVerificationFromIdentityVerificationId, failVerification } =
    useAccountOpenFlow();

  const handleError = (error: Error) => {
    failVerification(error.message || '본인인증 처리 중 문제가 발생했습니다.');
    router.replace('/account-open-verify' as never);
  };

  const handleComplete = async (response: PortOneIdentityVerificationResponse) => {
    const identityVerificationId = response.identityVerificationId;

    if (identityVerificationId && identityVerificationId.length > 0) {
      await completeVerificationFromIdentityVerificationId(identityVerificationId);
      router.replace('/account-open-verify' as never);
      return;
    }

    failVerification(response.message || '본인인증이 취소되었거나 완료되지 않았습니다.');
    router.replace('/account-open-verify' as never);
  };

  return (
    <IdentityVerification
      request={requestRef.current}
      onComplete={handleComplete}
      onError={handleError}
      style={styles.container}
    />
  );
}
