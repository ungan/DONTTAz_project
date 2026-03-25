import React from 'react';
import { View, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { PageHeader } from '@/shared/ui';
import { useTransferStore } from '@/features/transfer';
import { styles } from './transfer-layout.styles';

interface TransferLayoutProps {
  amount: number;
  children: React.ReactNode;
}

export function TransferLayout({ amount, children }: TransferLayoutProps) {
  const { bankName, depositAccountNumber } = useTransferStore();

  const isValid = amount >= 1;
  const formattedAmount = amount.toLocaleString('ko-KR');

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="송금" />

      <View style={styles.recipientRow}>
        <Text style={styles.recipientBank}>{bankName}</Text>
        <Text style={styles.recipientAccount}>{depositAccountNumber}</Text>
      </View>

      <View style={styles.amountArea}>
        {isValid ? (
          <View style={styles.amountRow}>
            <Text style={styles.amountText}>{formattedAmount}</Text>
            <Text style={styles.amountUnit}>원</Text>
          </View>
        ) : (
          <View style={styles.amountRow}>
            <Text style={styles.amountPlaceholder}>0</Text>
            <Text style={styles.amountUnitPlaceholder}>원</Text>
          </View>
        )}
      </View>

      {children}

    </SafeAreaView>
  );
}
