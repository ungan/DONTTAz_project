import React from 'react';
import { View, ScrollView, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { PageHeader } from '@/shared/ui';
import { useVault } from '@/entities/vault';
import { VaultBalance } from '@/widgets/vault-balance';
import { VaultNotice } from '@/widgets/vault-notice';
import { VaultHistory } from '@/widgets/vault-history';
import { styles } from './vault-page.styles';

export function VaultPage() {
  const router = useRouter();
  const { vault, transactions, isPending } = useVault();

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="가상금고" />
      {isPending || !vault ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#5899FD" />
        </View>
      ) : (
        <ScrollView showsVerticalScrollIndicator={false}>
          <VaultBalance
            lockedAmount={vault.lockedAmount}
            onDeposit={() => {}}
            onWithdraw={() => {}}
            onWishlistPress={() => router.push('/vault-wishlist')}
          />
          <VaultNotice
            baseInterestRate={vault.baseInterestRate}
            penaltyInterestRate={vault.penaltyInterestRate}
          />
          <VaultHistory transactions={transactions} />
        </ScrollView>
      )}
    </SafeAreaView>
  );
}
