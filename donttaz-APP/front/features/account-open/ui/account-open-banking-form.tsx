import React from 'react';
import { View, Text } from 'react-native';
import { CustomInput, SelectInput } from '@/shared/ui';
import { styles } from './account-open-banking-form.styles';

const DAILY_LIMIT_OPTIONS = ['100만원', '500만원', '1000만원', '5000만원'];

interface AccountOpenBankingFormProps {
  bankingId: string;
  bankingPassword: string;
  bankingPasswordConfirm: string;
  dailyTransferLimit: string;
  bankingIdError: string | null;
  bankingPasswordError: string | null;
  bankingPasswordConfirmError: string | null;
  dailyTransferLimitError: string | null;
  onBankingIdChange: (value: string) => void;
  onBankingPasswordChange: (value: string) => void;
  onBankingPasswordConfirmChange: (value: string) => void;
  onDailyTransferLimitChange: (value: string) => void;
}

export function AccountOpenBankingForm({
  bankingId,
  bankingPassword,
  bankingPasswordConfirm,
  dailyTransferLimit,
  bankingIdError,
  bankingPasswordError,
  bankingPasswordConfirmError,
  dailyTransferLimitError,
  onBankingIdChange,
  onBankingPasswordChange,
  onBankingPasswordConfirmChange,
  onDailyTransferLimitChange,
}: AccountOpenBankingFormProps) {
  return (
    <View style={styles.container}>
      <CustomInput
        label="뱅킹 ID"
        value={bankingId}
        onChangeText={onBankingIdChange}
        placeholder="영문/숫자 6~12자"
        autoCapitalize="none"
        errorMessage={bankingIdError ?? undefined}
      />

      <Text style={styles.helperText}>
        영문 소문자 또는 숫자를 사용해 6~12자로 입력해 주세요.
      </Text>

      <CustomInput
        label="뱅킹 비밀번호"
        value={bankingPassword}
        onChangeText={onBankingPasswordChange}
        placeholder="숫자 4자리"
        keyboardType="number-pad"
        secureTextEntry
        errorMessage={bankingPasswordError ?? undefined}
      />

      <CustomInput
        label="뱅킹 비밀번호 확인"
        value={bankingPasswordConfirm}
        onChangeText={onBankingPasswordConfirmChange}
        placeholder="숫자 4자리 재입력"
        keyboardType="number-pad"
        secureTextEntry
        errorMessage={bankingPasswordConfirmError ?? undefined}
      />

      <View style={styles.limitSection}>
        <Text style={styles.limitLabel}>1일 이체한도</Text>
        <SelectInput
          value={dailyTransferLimit}
          options={DAILY_LIMIT_OPTIONS}
          placeholder="한도 선택"
          onChange={onDailyTransferLimitChange}
        />
        {dailyTransferLimitError ? <Text style={styles.errorText}>{dailyTransferLimitError}</Text> : null}
      </View>
    </View>
  );
}
