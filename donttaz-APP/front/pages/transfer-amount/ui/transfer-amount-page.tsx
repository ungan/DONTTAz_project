import React from 'react';
import { View } from 'react-native';
import { useRouter } from 'expo-router';
import { NumericKeypad, Button } from '@/shared/ui';
import { TransferAmountSection, useTransferAmount, useTransferStore } from '@/features/transfer';
import { TransferLayout } from '@/widgets/transfer-layout';
import { styles } from './transfer-amount-page.styles';

export function TransferAmountPage() {
  const router = useRouter();
  const { setAmount } = useTransferStore();
  const { amount, isValid, handleKeyPress, handleQuickAdd } = useTransferAmount();

  const handleNext = () => {
    setAmount(amount);
    router.push('/transfer-memo');
  };

  return (
    <TransferLayout amount={amount}>
      <View style={styles.bottomArea}>
        <TransferAmountSection onQuickAdd={handleQuickAdd} />
        <NumericKeypad onKeyPress={handleKeyPress} />
        <View style={styles.footer}>
          <Button label="다음" disabled={!isValid} onPress={handleNext} />
        </View>
      </View>
    </TransferLayout>
  );
}
