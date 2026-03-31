import React from 'react';
import { View, Text, FlatList } from 'react-native';
import type { VaultTransaction } from '@/entities/vault';
import { styles } from './vault-history.styles';

const LABEL_MAP: Record<string, string> = {
  DEPOSIT: '가상금고에 보관',
  WITHDRAW_BROKEN: '긴급 출금',
};

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${mm}.${dd}`;
}

interface VaultHistoryProps {
  transactions: VaultTransaction[];
}

export function VaultHistory({ transactions }: VaultHistoryProps) {
  const isDeposit = (type: string) => type === 'DEPOSIT';
  const getLabel = (type: string) => LABEL_MAP[type] ?? '출금';
  const sorted = [...transactions].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  );

  return (
    <View style={styles.container}>
      <Text style={styles.sectionTitle}>거래 내역</Text>
      <FlatList
        data={sorted}
        keyExtractor={(item) => String(item.id)}
        scrollEnabled={false}
        ItemSeparatorComponent={() => <View style={styles.separator} />}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>거래 내역이 없습니다.</Text>
          </View>
        }
        renderItem={({ item }) => (
          <View style={styles.row}>
            <Text style={styles.date}>{formatDate(item.createdAt)}</Text>
            <Text style={styles.label}>{getLabel(item.transactionType)}</Text>
            <Text style={[styles.amount, isDeposit(item.transactionType) && styles.amountDeposit]}>
              {isDeposit(item.transactionType) ? '' : '-'}
              {item.amount.toLocaleString('ko-KR')}원
            </Text>
          </View>
        )}
      />
    </View>
  );
}
