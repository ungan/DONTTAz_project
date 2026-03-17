import React from 'react';
import { View } from 'react-native';
import { SelectInput, CustomInput } from '@/shared/ui';
import { BANK_OPTIONS } from '../types/transfer';
import type { TransferFormValues } from '../types/transfer';
import { styles } from './transfer-form-section.styles';

interface TransferFormSectionProps {
  form: TransferFormValues;
  onBankChange: (bank: string) => void;
  onAccountNumberChange: (value: string) => void;
}

export function TransferFormSection({
  form,
  onBankChange,
  onAccountNumberChange,
}: TransferFormSectionProps) {
  return (
    <View style={styles.container}>
      <SelectInput
        label="은행명"
        options={[...BANK_OPTIONS]}
        value={form.bankName}
        placeholder="국민은행"
        onChange={onBankChange}
      />
      <CustomInput
        label="계좌번호"
        placeholder="123-4567-890123"
        value={form.accountNumber}
        onChangeText={onAccountNumberChange}
        keyboardType="number-pad"
      />
    </View>
  );
}
