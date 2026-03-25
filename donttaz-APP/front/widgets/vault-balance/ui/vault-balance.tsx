import React from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { styles } from './vault-balance.styles';

const vaultAndChainImage = require('@/shared/assets/images/vault-and-chain.png');

interface VaultBalanceProps {
  lockedAmount: number;
  onDeposit: () => void;
  onWithdraw: () => void;
  variant?: 'default' | 'wishlist';
}

export function VaultBalance({ lockedAmount, onDeposit, onWithdraw, variant = 'default' }: VaultBalanceProps) {
  if (variant === 'wishlist') return null;

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
        <TouchableOpacity style={[styles.button, styles.buttonPrimary]} onPress={onDeposit} activeOpacity={0.8}>
          <Text style={[styles.buttonText, styles.buttonTextPrimary]}>+ 넣기</Text>
        </TouchableOpacity>
        <TouchableOpacity style={[styles.button, styles.buttonOutline]} onPress={onWithdraw} activeOpacity={0.8}>
          <Text style={[styles.buttonText, styles.buttonTextOutline]}>긴급 출금</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}
