import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { usePayments } from '@/entities/payment';
import { useBankAccountStore } from '@/entities/bank-account';
import { styles } from './payment-history.styles';

const CATEGORY_ICON_MAP: Record<string, { icon: keyof typeof Ionicons.glyphMap; color: string }> = {
  '식비/카페': { icon: 'cafe-outline', color: '#F59E0B' },
  '식비': { icon: 'restaurant-outline', color: '#F97316' },
  '편의점/마트': { icon: 'storefront-outline', color: '#10B981' },
  '교통': { icon: 'bus-outline', color: '#3B82F6' },
  '쇼핑': { icon: 'bag-outline', color: '#EC4899' },
  '기타/이벤트': { icon: 'gift-outline', color: '#8B5CF6' },
};

const DEFAULT_ICON = { icon: 'card-outline' as keyof typeof Ionicons.glyphMap, color: '#6B7280' };

interface PaymentHistoryProps {
  onViewAllPress?: () => void;
}

export function PaymentHistory({ onViewAllPress }: PaymentHistoryProps) {
  const { accountId } = useBankAccountStore();
  const { transactions, isPending } = usePayments();

  if (!accountId) {
    return (
      <View style={styles.card}>
        <View style={styles.header}>
          <Text style={styles.title}>결제내역</Text>
        </View>
        <Text style={styles.emptyText}>계좌 정보가 없습니다.</Text>
      </View>
    );
  }

  return (
    <View style={styles.card}>
      <View style={styles.header}>
        <Text style={styles.title}>결제내역</Text>
        <TouchableOpacity style={styles.viewAll} onPress={onViewAllPress}>
          <Text style={styles.viewAllText}>전체 결제내역</Text>
          <Ionicons name="chevron-forward" size={16} color="#101828" />
        </TouchableOpacity>
      </View>

      <View style={styles.list}>
        {isPending ? (
          <Text style={styles.emptyText}>불러오는 중...</Text>
        ) : (
          transactions.slice(0, 3).map((item) => {
            const { icon, color } = CATEGORY_ICON_MAP[item.category] ?? DEFAULT_ICON;
            return (
              <View key={item.transactionId} style={styles.item}>
                <View style={[styles.iconBox, { backgroundColor: `${color}1A` }]}>
                  <Ionicons name={icon} size={18} color={color} />
                </View>
                <Text style={styles.merchantName}>{item.counterpartyName}</Text>
                <Text style={styles.amount}>
                  {item.type === 'WITHDRAWAL' ? '-' : '+'}
                  {item.amount.toLocaleString()}원
                </Text>
              </View>
            );
          })
        )}
      </View>
    </View>
  );
}
