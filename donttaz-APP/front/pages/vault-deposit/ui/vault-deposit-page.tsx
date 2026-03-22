import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { NumericKeypad } from '@/shared/ui';
import { useDepositAmount } from '@/features/vault-deposit';
import { styles } from './vault-deposit-page.styles';

export function VaultDepositPage() {
  const router = useRouter();
  const { lockedAmount } = useLocalSearchParams<{ lockedAmount: string }>();

  const { formattedAmount, isValid, handleKeyPress } = useDepositAmount();

  const currentBalance = Number(lockedAmount ?? 0);

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
          style={[styles.confirmButton, !isValid && styles.confirmButtonDisabled]}
          disabled={!isValid}
          onPress={() => {
            // TODO: 입금 API 연결
            router.back();
          }}
          activeOpacity={0.85}
        >
          <Text style={[styles.confirmText, !isValid && styles.confirmTextDisabled]}>확인</Text>
        </TouchableOpacity>
        <NumericKeypad onKeyPress={handleKeyPress} />
      </View>
    </SafeAreaView>
  );
}
