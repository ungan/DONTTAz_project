import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './vault-balance.styles';

const vaultAndChainImage = require('@/shared/assets/images/vault-and-chain.png');

interface VaultBalanceProps {
  lockedAmount: number;
  onDeposit: () => void;
  onWithdraw: () => void;
}

export function VaultBalance({ lockedAmount, onDeposit, onWithdraw }: VaultBalanceProps) {
  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <View style={styles.labelLeft}>
          <Ionicons name="lock-closed" size={18} color="#111827" />
          <Text style={styles.label}>지켜낸 돈</Text>
        </View>
      </View>

      <Text style={styles.amount}>{lockedAmount.toLocaleString('ko-KR')}원</Text>

      <View style={styles.imageContainer}>
        <Image source={vaultAndChainImage} style={styles.vaultImage} resizeMode="contain" />
      </View>

      <View style={styles.buttonRow}>
        <TouchableOpacity style={styles.button} onPress={onDeposit} activeOpacity={0.7}>
          <Text style={styles.buttonText}>+ 넣기</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={onWithdraw} activeOpacity={0.7}>
          <Text style={styles.buttonText}>— 긴급 출금</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
