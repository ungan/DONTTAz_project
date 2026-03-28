import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { PageHeader, NumericKeypad } from '@/shared/ui';
import { useDepositAmount } from '@/features/vault-deposit';
import { useMyAccounts } from '@/entities/account';
import { depositVaultApi } from '@/entities/vault';
import type { VaultKind } from '@/entities/vault';
import { styles } from './vault-deposit-page.styles';

export function VaultDepositPage() {
  const router = useRouter();
  const { kind, vaultId, lockedAmount, goalAmount } = useLocalSearchParams<{
    kind: string;
    vaultId: string;
    lockedAmount: string;
    goalAmount: string;
  }>();

  const { accounts } = useMyAccounts();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const currentBalance = Number(lockedAmount ?? 0);
  const maxDeposit = goalAmount ? Number(goalAmount) - currentBalance : 100_000_000;
  const { amount, formattedAmount, isValid, handleKeyPress } = useDepositAmount(maxDeposit);
  const primaryAccount = accounts.find((a) => a.isPrimary) ?? accounts[0];

  const handleConfirm = async () => {
    if (!primaryAccount) {
      Alert.alert('오류', '연결된 계좌가 없습니다.');
      return;
    }
    const requestBody = { amount };
    setIsSubmitting(true);
    try {
      await depositVaultApi(kind as VaultKind, Number(vaultId), requestBody);
      router.back();
    } catch {
      Alert.alert('오류', '입금 중 문제가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* 취소 */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.cancelButton} onPress={() => router.back()} activeOpacity={0.7}>
          <Text style={styles.cancelText}>취소</Text>
        </TouchableOpacity>
      </View>

      {/* 금액 입력 */}
      <View style={styles.amountArea}>
        <Text style={styles.currentBalanceLabel}>지켜낸 돈</Text>
        <Text style={styles.currentBalanceAmount}>
          {currentBalance.toLocaleString('ko-KR')}원
        </Text>

        <View style={styles.inputRow}>
          <Text style={[styles.plus, !isValid && styles.dimmed]}>+</Text>
          <Text style={[styles.inputAmount, !isValid && styles.inputAmountEmpty]}>
            {isValid ? formattedAmount : '0'}
          </Text>
          {isValid && (
            <TouchableOpacity style={styles.clearButton} onPress={() => handleKeyPress('X')} activeOpacity={0.7}>
              <Text style={styles.clearIcon}>×</Text>
            </TouchableOpacity>
          )}
          <Text style={[styles.inputUnit, !isValid && styles.dimmed]}>원</Text>
        </View>
      </View>

      {/* 확인 + 키패드 */}
      <View style={styles.bottomArea}>
        <TouchableOpacity
          style={[styles.confirmButton, (!isValid || isSubmitting) && styles.confirmButtonDisabled]}
          disabled={!isValid || isSubmitting}
          onPress={handleConfirm}
          activeOpacity={0.85}
        >
          <Text style={[styles.confirmText, (!isValid || isSubmitting) && styles.confirmTextDisabled]}>확인</Text>
        </TouchableOpacity>
        <NumericKeypad onKeyPress={handleKeyPress} />
      </View>
    </SafeAreaView>
  );
}
