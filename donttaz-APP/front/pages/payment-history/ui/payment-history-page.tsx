import { useEffect, useState } from 'react';
import { View, Text, FlatList, ActivityIndicator, TouchableOpacity } from 'react-native';
import * as Clipboard from 'expo-clipboard';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { usePaymentsInfinite } from '@/entities/payment';
import { useBankAccountStore } from '@/entities/bank-account';
import { PageHeader } from '@/shared/ui';
import type { Transaction } from '@/entities/payment';
import { styles } from './payment-history-page.styles';

const CATEGORY_ICON_MAP: Record<string, { icon: keyof typeof Ionicons.glyphMap; color: string }> = {
  '식비/카페': { icon: 'cafe-outline', color: '#F59E0B' },
  식비: { icon: 'restaurant-outline', color: '#F97316' },
  '편의점/마트': { icon: 'storefront-outline', color: '#10B981' },
  교통: { icon: 'bus-outline', color: '#3B82F6' },
  쇼핑: { icon: 'bag-outline', color: '#EC4899' },
  '기타/이벤트': { icon: 'gift-outline', color: '#8B5CF6' },
};

const DEFAULT_ICON = { icon: 'card-outline' as keyof typeof Ionicons.glyphMap, color: '#6B7280' };

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}.${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`;
}

function TransactionItem({ item }: { item: Transaction }) {
  const { icon, color } = CATEGORY_ICON_MAP[item.category] ?? DEFAULT_ICON;
  const isWithdrawal = item.type === 'WITHDRAWAL';

  return (
    <View style={styles.item}>
      <View style={[styles.iconBox, { backgroundColor: `${color}1A` }]}>
        <Ionicons name={icon} size={20} color={color} />
      </View>
      <View style={styles.infoGroup}>
        <Text style={styles.counterparty}>{item.counterpartyName}</Text>
        <Text style={styles.itemDate}>{formatDate(item.transactionDate)}</Text>
      </View>
      <View style={styles.amountGroup}>
        <Text style={isWithdrawal ? styles.amountWithdrawal : styles.amountDeposit}>
          {isWithdrawal ? '-' : '+'}{item.amount.toLocaleString()}원
        </Text>
        <Text style={styles.balanceAfter}>{item.balanceAfter.toLocaleString()}원</Text>
      </View>
    </View>
  );
}

export function PaymentHistoryPage() {
  const { transactions, isPending, isRefreshing, isLast, error, loadFirst, loadNext } =
    usePaymentsInfinite();
  const { accountNo, balance, accountName } = useBankAccountStore();
  const [copied, setCopied] = useState(false);
  const router = useRouter();

  useEffect(() => {
    loadFirst();
  }, [loadFirst]);

  const handleCopyAccountNo = async () => {
    if (!accountNo) return;
    await Clipboard.setStringAsync(accountNo);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <View style={styles.container}>
      <PageHeader title="전체 결제내역" />

      <View style={styles.accountSection}>
        {accountNo ? (
          <View>
            <TouchableOpacity onPress={handleCopyAccountNo} activeOpacity={0.7}>
              <Text style={styles.accountNo}>{accountNo}</Text>
            </TouchableOpacity>
            {copied ? (
              <View style={styles.copyToast}>
                <Text style={styles.copyToastText}>복사됨</Text>
              </View>
            ) : null}
          </View>
        ) : null}
        <Text style={styles.accountBalance}>
          {(balance ?? 0).toLocaleString()}원
        </Text>
        <TouchableOpacity
          style={styles.transferButton}
          onPress={() => router.push('/transfer-account')}
          activeOpacity={0.7}
        >
          <Text style={styles.transferButtonText}>이체하기</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.listArea}>
        {error && transactions.length === 0 ? (
          <Text style={styles.errorText}>{error}</Text>
        ) : (
          <FlatList
            data={transactions}
            keyExtractor={item => String(item.transactionId)}
            renderItem={({ item }) => <TransactionItem item={item} />}
            contentContainerStyle={[
              styles.list,
              transactions.length === 0 && styles.emptyContainer,
            ]}
            onEndReached={loadNext}
            onEndReachedThreshold={0.3}
            onRefresh={loadFirst}
            refreshing={isRefreshing}
            ListEmptyComponent={
              !isPending ? <Text style={styles.emptyText}>거래내역이 없습니다.</Text> : null
            }
            ListFooterComponent={
              isPending ? (
                <View style={styles.footer}>
                  <ActivityIndicator size="small" color="#5899FD" />
                </View>
              ) : isLast && transactions.length > 0 ? (
                <View style={styles.footer}>
                  <Text style={styles.footerText}>모든 거래내역을 불러왔습니다.</Text>
                </View>
              ) : null
            }
          />
        )}
      </View>
    </View>
  );
}
