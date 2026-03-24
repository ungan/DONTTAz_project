import React, { useState } from 'react';
import { View, Text, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import type { Vault, VaultTransaction } from '@/entities/vault';
import { WithdrawWarningModal } from '@/features/vault-withdraw';
import { VaultBalance } from '@/widgets/vault-balance';
import { VaultNotice } from '@/widgets/vault-notice';
import { VaultHistory } from '@/widgets/vault-history';
import { styles } from './vault-page.styles';

interface VaultDefaultTabProps {
  vault: Vault | null;
  transactions: VaultTransaction[];
}

export function VaultDefaultTab({ vault, transactions }: VaultDefaultTabProps) {
  const router = useRouter();
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  if (!vault) {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyText}>기본 금고가 없습니다.</Text>
      </View>
    );
  }

  return (
    <>
      <ScrollView showsVerticalScrollIndicator={false}>
        <VaultBalance
          lockedAmount={vault.lockedAmount}
          onDeposit={() => router.push(`/vault-deposit?kind=DEFAULT&vaultId=${vault.vaultId}&lockedAmount=${vault.lockedAmount}`)}
          onWithdraw={() => setShowWithdrawModal(true)}
        />
        <VaultNotice
          baseInterestRate={vault.baseInterestRate}
          penaltyInterestRate={vault.penaltyInterestRate}
        />
        <VaultHistory transactions={transactions} />
      </ScrollView>

      <WithdrawWarningModal
        visible={showWithdrawModal}
        baseInterestRate={vault.baseInterestRate}
        penaltyRate={vault.penaltyInterestRate}
        onCancel={() => setShowWithdrawModal(false)}
        onConfirm={() => {
          setShowWithdrawModal(false);
          // TODO: 출금 API 연결
        }}
      />
    </>
  );
}
