import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { styles } from './transfer-confirm-page.styles';

export function TransferConfirmPage() {
  const router = useRouter();
  const { depositAccountName, depositBankName, depositAccountNumber, amount } =
    useLocalSearchParams<{
      depositAccountName: string;
      depositBankName: string;
      depositAccountNumber: string;
      amount: string;
    }>();

  const formattedAmount = Number(amount).toLocaleString('ko-KR');

  const handleExtra = () => {
    router.replace('/transfer-account');
  };

  const handleConfirm = () => {
    router.replace('/(tabs)/home');
  };

  return (
    <SafeAreaView style={styles.container}>
      <Image
        source={require('@/shared/assets/icons/checked.png')}
        style={styles.image}
        resizeMode="contain"
      />

      <Text style={styles.amountText}>
        <Text style={styles.amountNameText}>{depositAccountName}</Text>
        {`에게 ${formattedAmount}원을\n보냈어요`}
      </Text>

      <View style={styles.accountSection}>
        <Text style={styles.accountLabel}>받는 계좌</Text>
        <Text style={styles.accountValue}>{`${depositBankName} ${depositAccountNumber}`}</Text>
      </View>

      <View style={styles.buttonRow}>
        <TouchableOpacity style={styles.extraButton} onPress={handleExtra} activeOpacity={0.8}>
          <Text style={styles.extraButtonLabel}>추가이체</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.confirmButton} onPress={handleConfirm} activeOpacity={0.8}>
          <Text style={styles.confirmButtonLabel}>확인</Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}
