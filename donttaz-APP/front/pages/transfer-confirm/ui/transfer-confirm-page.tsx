import React, { useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, Image, Animated } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { styles } from './transfer-confirm-page.styles';

const checkedImage = require('@/shared/assets/icons/checked.png');

export function TransferConfirmPage() {
  const router = useRouter();
  const scale = useRef(new Animated.Value(0.3)).current;
  const opacity = useRef(new Animated.Value(0)).current;

  const { depositBankName, depositAccountNumber, amount, depositMemo } =
    useLocalSearchParams<{
      depositBankName: string;
      depositAccountNumber: string;
      amount: string;
      depositMemo?: string;
    }>();

  useEffect(() => {
    Animated.parallel([
      Animated.spring(scale, {
        toValue: 1,
        friction: 5,
        tension: 80,
        useNativeDriver: true,
      }),
      Animated.timing(opacity, {
        toValue: 1,
        duration: 400,
        useNativeDriver: true,
      }),
    ]).start();
  }, []);

  const formattedAmount = Number(amount).toLocaleString('ko-KR');
  const recipientLabel = depositMemo?.trim() || depositBankName;

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.topSection}>
        <Animated.Image
          source={checkedImage}
          style={[styles.icon, { transform: [{ scale }] }]}
          resizeMode="contain"
        />
        <Animated.View style={[styles.textArea, { opacity }]}>
          <Text style={styles.amountText}>{formattedAmount}원</Text>
          <Text style={styles.recipientText}>{recipientLabel}에게{'\n'}보냈어요</Text>
        </Animated.View>
      </View>

      <Animated.View style={[styles.detailCard, { opacity }]}>
        {depositMemo?.trim() ? (
          <>
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>받는 분</Text>
              <Text style={styles.detailValue}>{depositMemo.trim()}</Text>
            </View>
            <View style={styles.detailDivider} />
          </>
        ) : null}
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>받는 계좌</Text>
          <Text style={styles.detailValue}>{depositBankName} {depositAccountNumber}</Text>
        </View>
        <View style={styles.detailDivider} />
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>금액</Text>
          <Text style={styles.detailValue}>{formattedAmount}원</Text>
        </View>
      </Animated.View>

      <Animated.View style={[styles.buttonArea, { opacity }]}>
        <TouchableOpacity style={styles.confirmButton} onPress={() => router.replace('/(tabs)/home')} activeOpacity={0.8}>
          <Text style={styles.confirmButtonLabel}>확인</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.extraButton} onPress={() => router.replace('/transfer-account')} activeOpacity={0.7}>
          <Text style={styles.extraButtonLabel}>추가 이체</Text>
        </TouchableOpacity>
      </Animated.View>
    </SafeAreaView>
  );
}
