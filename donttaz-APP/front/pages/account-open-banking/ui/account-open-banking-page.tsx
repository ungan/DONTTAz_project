import React, { useEffect } from 'react';
import { ScrollView, View, Text, KeyboardAvoidingView } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenBankingForm,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-banking-page.styles';

export function AccountOpenBankingPage() {
  const insets = useSafeAreaInsets();
  const {
    bankingId,
    bankingPassword,
    bankingPasswordConfirm,
    dailyTransferLimit,
    bankingIdError,
    bankingPasswordError,
    bankingPasswordConfirmError,
    dailyTransferLimitError,
    verifiedOwnerName,
    moveToBankingStep,
    updateBankingId,
    updateBankingPassword,
    updateBankingPasswordConfirm,
    updateDailyTransferLimit,
    submitBankingStep,
  } = useAccountOpenFlow();

  useEffect(() => {
    if (!verifiedOwnerName) {
      router.replace('/account-open-verify' as never);
      return;
    }

    moveToBankingStep();
  }, [verifiedOwnerName]);

  return (
    <View style={styles.outerContainer}>
      <View style={styles.headerBlock}>
        <PageHeader title="전자금융 설정" onBack={() => router.replace('/account-open-verify' as never)} />
        <AccountOpenProgress currentStep="banking" />
      </View>

      <KeyboardAvoidingView behavior="padding" style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.heroSection}>
          <Text style={styles.title}>전자금융 설정</Text>
          <Text style={styles.description}>
            뱅킹 ID, 비밀번호, 이체 한도를 설정합니다.
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
      </ScrollView>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        <Button label="계좌 개설하기" onPress={submitBankingStep} />
      </View>
      </KeyboardAvoidingView>
    </View>
  );
}
