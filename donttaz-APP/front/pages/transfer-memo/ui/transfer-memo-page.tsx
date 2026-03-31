import React, { useRef, useState } from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { Button } from '@/shared/ui';
import { useTransferStore, TransferPinSheet } from '@/features/transfer';
import { TransferLayout } from '@/widgets/transfer-layout';

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
          <View style={styles.card}>
            <View style={styles.row}>
              <Text style={styles.rowLabel}>받는 분 통장</Text>
              <TextInput
                style={styles.rowInput}
                placeholder="미입력 시 내 이름"
                placeholderTextColor="#C4C9D4"
                value={depositMemo}
                onChangeText={setDepositMemo}
                maxLength={20}
                returnKeyType="next"
              />
            </View>
            <View style={styles.divider} />
            <View style={styles.row}>
              <Text style={styles.rowLabel}>내 통장</Text>
              <TextInput
                style={styles.rowInput}
                placeholder="미입력 시 수취인명"
                placeholderTextColor="#C4C9D4"
                value={withdrawalMemo}
                onChangeText={setWithdrawalMemo}
                maxLength={20}
                returnKeyType="done"
              />
            </View>
          </View>
        </View>
        <View style={styles.footer}>
          <Button label="확인" onPress={handleNext} />
        </View>
      </TransferLayout>
      <TransferPinSheet ref={pinSheetRef} />
    </>
  );
}

const styles = StyleSheet.create({
  bottomArea: {
    paddingHorizontal: 24,
  },
  card: {
    borderRadius: 16,
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#F3F4F6',
    overflow: 'hidden',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
    gap: 12,
  },
  rowLabel: {
    fontSize: 15,
    fontWeight: '500',
    color: '#6B7280',
    width: 100,
  },
  rowInput: {
    flex: 1,
    fontSize: 15,
    fontWeight: '400',
    color: '#111827',
    textAlign: 'right',
    padding: 0,
  },
  divider: {
    height: 1,
    backgroundColor: '#F3F4F6',
    marginHorizontal: 20,
  },
  footer: {
    paddingHorizontal: 24,
    paddingTop: 16,
    paddingBottom: 16,
  },
});
