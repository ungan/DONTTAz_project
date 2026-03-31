import React from 'react';
import { View, Text, TouchableOpacity, ScrollView, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { CustomInput } from '@/shared/ui';
import type { TransferFormValues } from '../types/transfer';
import { useRecentAccounts } from '../model/use-recent-accounts';

interface TransferFormSectionProps {
  form: TransferFormValues;
  onBankChange: (bank: string) => void;
  onAccountNumberChange: (value: string) => void;
  onOpenBankPicker: () => void;
}

export function TransferFormSection({
  form,
  onBankChange,
  onAccountNumberChange,
  onOpenBankPicker,
}: TransferFormSectionProps) {
  const recentAccounts = useRecentAccounts();

  const handleSelectRecent = (bankName: string, accountNumber: string) => {
    onBankChange(bankName);
    onAccountNumberChange(accountNumber);
  };

  return (
    <View style={styles.container}>
      <View style={styles.field}>
        <Text style={styles.label}>은행</Text>
        <TouchableOpacity
          style={styles.selector}
          onPress={onOpenBankPicker}
          activeOpacity={0.7}
        >
          <Text style={[styles.selectorText, !form.bankName && styles.placeholder]}>
            {form.bankName || '은행을 선택하세요'}
          </Text>
          <Ionicons name="chevron-down" size={18} color="#9CA3AF" />
        </TouchableOpacity>
      </View>

      <CustomInput
        label="계좌번호"
        placeholder="계좌번호를 입력하세요"
        value={form.accountNumber}
        onChangeText={onAccountNumberChange}
        keyboardType="number-pad"
      />

      {recentAccounts.length > 0 && (
        <View style={styles.recentSection}>
          <Text style={styles.recentLabel}>최근 이체</Text>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.chipList}
          >
            {recentAccounts.map((account, index) => (
              <TouchableOpacity
                key={index}
                style={styles.chip}
                onPress={() => handleSelectRecent(account.bankName, account.accountNumber)}
                activeOpacity={0.7}
              >
                <Text style={styles.chipBank}>{account.bankName}</Text>
                <Text style={styles.chipAccount}>
                  {account.accountNumber.slice(0, 4)}·····{account.accountNumber.slice(-4)}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: 16,
  },
  field: {
    gap: 6,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#374151',
  },
  selector: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    height: 52,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#FAFAFA',
    paddingHorizontal: 16,
  },
  selectorText: {
    fontSize: 15,
    fontWeight: '500',
    color: '#111827',
  },
  placeholder: {
    color: '#9CA3AF',
    fontWeight: '400',
  },
  recentSection: {
    gap: 10,
    marginTop: 16,
  },
  recentLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#9CA3AF',
    letterSpacing: 0.3,
  },
  chipList: {
    gap: 8,
    paddingVertical: 2,
  },
  chip: {
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 12,
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#F3F4F6',
    gap: 2,
  },
  chipBank: {
    fontSize: 12,
    fontWeight: '600',
    color: '#374151',
  },
  chipAccount: {
    fontSize: 12,
    color: '#9CA3AF',
  },
});
