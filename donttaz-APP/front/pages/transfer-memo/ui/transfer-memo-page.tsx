import React, { useRef, useState } from 'react';
import { View } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { InlineInput, Button } from '@/shared/ui';
import { useTransferStore, TransferPinSheet } from '@/features/transfer';
import { TransferLayout } from '@/widgets/transfer-layout';
import { styles } from './transfer-memo-page.styles';

export function TransferMemoPage() {
  const pinSheetRef = useRef<BottomSheet>(null);
  const [withdrawalMemo, setWithdrawalMemo] = useState('');
  const [depositMemo, setDepositMemo] = useState('');
  const { amount, setMemo } = useTransferStore();

  const handleNext = () => {
    setMemo(withdrawalMemo, depositMemo);
    pinSheetRef.current?.expand();
  };

  return (
    <>
      <TransferLayout amount={amount}>
        <View style={styles.bottomArea}>
          <InlineInput
            label="받는 분 통장 메모"
            placeholder="미입력시 내 이름"
            value={depositMemo}
            onChangeText={setDepositMemo}
            maxLength={20}
          />
          <InlineInput
            label="내 통장 메모"
            placeholder="미입력시 수취인명"
            value={withdrawalMemo}
            onChangeText={setWithdrawalMemo}
            maxLength={20}
          />
        </View>
        <View style={styles.footer}>
          <Button label="다음" onPress={handleNext} />
        </View>
      </TransferLayout>
      <TransferPinSheet ref={pinSheetRef} />
    </>
  );
}
