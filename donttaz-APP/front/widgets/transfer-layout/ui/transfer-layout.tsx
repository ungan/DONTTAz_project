import React, { useState } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { PageHeader, ConfirmModal } from '@/shared/ui';
import { useTransferStore } from '@/features/transfer';
import { styles } from './transfer-layout.styles';

interface TransferLayoutProps {
  amount: number;
  children: React.ReactNode;
}

export function TransferLayout({ amount, children }: TransferLayoutProps) {
  const router = useRouter();
  const [cancelModalVisible, setCancelModalVisible] = useState(false);
  const { bankName, depositAccountNumber } = useTransferStore();

  const isValid = amount >= 1;
  const formattedAmount = amount.toLocaleString('ko-KR');

  const handleCancelConfirm = () => {
    useTransferStore.getState().clear();
    router.push('/(tabs)/home');
  };

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader
        title="송금"
        rightAction={
          <TouchableOpacity onPress={() => setCancelModalVisible(true)} activeOpacity={0.7}>
            <Text style={styles.cancelLabel}>취소</Text>
          </TouchableOpacity>
        }
      />

      <Text style={styles.accountSummary}>{bankName} {depositAccountNumber}</Text>

      <View style={styles.amountArea}>
        <Text style={isValid ? styles.amountText : styles.amountPlaceholder}>
          {isValid ? `${formattedAmount}원` : '보낼금액'}
        </Text>
      </View>

      {children}

      <ConfirmModal
        visible={cancelModalVisible}
        title="이체를 취소하겠습니까?"
        confirmLabel="취소하기"
        cancelLabel="계속하기"
        onConfirm={handleCancelConfirm}
        onCancel={() => setCancelModalVisible(false)}
      />
    </SafeAreaView>
  );
}
