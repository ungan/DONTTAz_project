import React from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import { styles } from './transfer-source-account-section.styles';

export interface TransferSourceAccountOption {
  id: string;
  bankName: string;
  accountName: string;
  accountLabel: string;
  balanceLabel: string;
  isPrimary: boolean;
  isSelectable: boolean;
}

interface TransferSourceAccountSectionProps {
  accounts: TransferSourceAccountOption[];
  selectedAccountId: string | null;
  isPending: boolean;
  error: string | null;
  onSelect: (accountId: string) => void;
}

export function TransferSourceAccountSection({
  accounts,
  selectedAccountId,
  isPending,
  error,
  onSelect,
}: TransferSourceAccountSectionProps) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>출금계좌</Text>

      {isPending ? <Text style={styles.emptyText}>내 계좌를 불러오는 중...</Text> : null}
      {!isPending && error ? <Text style={styles.warningText}>{error}</Text> : null}
      {!isPending && !error && accounts.length === 0 ? (
        <Text style={styles.emptyText}>선택할 수 있는 계좌가 없습니다.</Text>
      ) : null}

      <View style={styles.cardList}>
        {accounts.map((account) => {
          const isSelected = account.id === selectedAccountId;

          return (
            <TouchableOpacity
              key={account.id}
              activeOpacity={0.8}
              disabled={!account.isSelectable}
              onPress={() => onSelect(account.id)}
              style={[
                styles.card,
                isSelected && styles.selectedCard,
                !account.isSelectable && styles.unavailableCard,
              ]}
            >
              <View style={styles.row}>
                <Text style={styles.bankName}>{account.bankName}</Text>
                {account.isPrimary ? (
                  <View style={styles.badge}>
                    <Text style={styles.badgeText}>대표 계좌</Text>
                  </View>
                ) : null}
              </View>

              <Text style={styles.accountNumber}>{account.accountLabel}</Text>
              <Text style={styles.balance}>{account.balanceLabel}</Text>
              {!account.isSelectable ? (
                <Text style={styles.warningText}>
                  이 계좌는 현재 앱에서 출금 계좌번호를 확인할 수 없어 선택할 수 없습니다.
                </Text>
              ) : null}
            </TouchableOpacity>
          );
        })}
      </View>
    </View>
  );
}
