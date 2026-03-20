import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { View, Text, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { PageHeader, Button } from '@/shared/ui';
import { TransferFormSection, useTransferForm, useTransferStore, BANK_CODE_MAP } from '@/features/transfer';
import { styles } from './transfer-account-page.styles';

export function TransferAccountPage() {
  const router = useRouter();
  const { form, setBank, setAccountNumber, isValid } = useTransferForm();
  const { setAccountInfo } = useTransferStore();

  const handleConfirm = () => {
    const bankCode = BANK_CODE_MAP[form.bankName as keyof typeof BANK_CODE_MAP];
    setAccountInfo(form.bankName, bankCode, form.accountNumber);
    router.push('/transfer-amount');
  };

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="송금" />

      <KeyboardAvoidingView
        style={styles.keyboardAvoid}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        <View style={styles.content}>
          <Text style={styles.question}>어디로 돈을 보낼까요?</Text>

          <View style={styles.formAndButton}>
            <TransferFormSection
              form={form}
              onBankChange={setBank}
              onAccountNumberChange={setAccountNumber}
            />
            <Button label="확인" disabled={!isValid} onPress={handleConfirm} />
          </View>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
