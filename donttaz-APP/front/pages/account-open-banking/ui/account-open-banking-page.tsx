import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenBankingForm,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-banking-page.styles';

export function AccountOpenBankingPage() {
  const {
    bankingId,
    bankingPassword,
    bankingPasswordConfirm,
    dailyTransferLimit,
    bankingIdError,
    bankingPasswordError,
    bankingPasswordConfirmError,
    dailyTransferLimitError,
    emailLocalPart,
    emailDomain,
    moveToBankingStep,
    updateBankingId,
    updateBankingPassword,
    updateBankingPasswordConfirm,
    updateDailyTransferLimit,
    submitBankingStep,
  } = useAccountOpenFlow();

  useEffect(() => {
    if (!emailLocalPart || !emailDomain) {
      router.replace('/account-open-profile' as never);
      return;
    }

    moveToBankingStep();
  }, [emailLocalPart, emailDomain]);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="계좌정보 입력" onBack={() => router.replace('/account-open-profile' as never)} />

      <AccountOpenProgress currentStep="banking" />

      <View style={styles.heroSection}>
        <Text style={styles.title}>전자금융 신청 정보를 입력해 주세요.</Text>
        <Text style={styles.description}>
          계좌 개설 후 사용할 뱅킹 ID와 비밀번호, 이체 한도를 설정한 뒤 바로 계좌를 생성합니다.
        </Text>
      </View>

      <AccountOpenBankingForm
        bankingId={bankingId}
        bankingPassword={bankingPassword}
        bankingPasswordConfirm={bankingPasswordConfirm}
        dailyTransferLimit={dailyTransferLimit}
        bankingIdError={bankingIdError}
        bankingPasswordError={bankingPasswordError}
        bankingPasswordConfirmError={bankingPasswordConfirmError}
        dailyTransferLimitError={dailyTransferLimitError}
        onBankingIdChange={updateBankingId}
        onBankingPasswordChange={updateBankingPassword}
        onBankingPasswordConfirmChange={updateBankingPasswordConfirm}
        onDailyTransferLimitChange={updateDailyTransferLimit}
      />

      <Button label="계좌 개설하기" onPress={submitBankingStep} />
    </ScrollView>
  );
}
