import React, { useState } from 'react';
import { View, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { PageHeader } from '@/shared/ui';
import { useVault } from '@/entities/vault';
import { VaultTabBar } from './vault-tab-bar';
import type { VaultTab } from './vault-tab-bar';
import { VaultDefaultTab } from './vault-default-tab';
import { VaultWishlistTab } from './vault-wishlist-tab';
import { styles } from './vault-page.styles';

export function VaultPage() {
  const router = useRouter();
  const { defaultVault, wishlistVault, transactions, wishlistTransactions, isPending } = useVault();
  const [activeTab, setActiveTab] = useState<VaultTab>('default');

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="금고" onBack={() => router.replace('/(tabs)/home')} />
      <VaultTabBar activeTab={activeTab} onTabChange={setActiveTab} />

      <View style={styles.content}>
        {isPending ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#5899FD" />
          </View>
        ) : activeTab === 'default' ? (
          <VaultDefaultTab vault={defaultVault} transactions={transactions} />
        ) : (
          <VaultWishlistTab vault={wishlistVault} transactions={wishlistTransactions} />
        )}
      </View>
    </SafeAreaView>
  );
}
