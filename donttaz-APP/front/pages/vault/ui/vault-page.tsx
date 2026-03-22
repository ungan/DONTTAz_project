import React, { useRef, useState } from 'react';
import { View, ScrollView, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import BottomSheet from '@gorhom/bottom-sheet';
import { PageHeader } from '@/shared/ui';
import { useVault, useWishlist } from '@/entities/vault';
import type { Wishlist, WishlistRequest } from '@/entities/vault';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import { WithdrawWarningModal } from '@/features/vault-withdraw';
import { VaultCarousel } from '@/widgets/vault-carousel';
import { VaultNotice } from '@/widgets/vault-notice';
import { VaultHistory } from '@/widgets/vault-history';
import { styles } from './vault-page.styles';

export function VaultPage() {
  const router = useRouter();
  const { vault, transactions, isPending } = useVault();
  const { wishlists, post, patch } = useWishlist(vault?.vaultId ?? null);

  const bottomSheetRef = useRef<BottomSheet>(null);
  const [editTarget, setEditTarget] = useState<Wishlist | undefined>();
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  const openBottomSheet = (target?: Wishlist) => {
    setEditTarget(target);
    bottomSheetRef.current?.expand();
  };

  const handleWishlistSubmit = async (body: WishlistRequest, wishId?: number) => {
    if (wishId) {
      await patch(wishId, body);
    } else {
      await post(body);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="가상 금고" onBack={() => router.replace('/(tabs)/home')} />
      {isPending || !vault ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#5899FD" />
        </View>
      ) : (
        <ScrollView showsVerticalScrollIndicator={false}>
          <VaultCarousel
            vault={vault}
            wishlists={wishlists}
            onDeposit={() => router.push(`/vault-deposit?lockedAmount=${vault.lockedAmount}`)}
            onWithdraw={() => setShowWithdrawModal(true)}
            onRegisterPress={() => openBottomSheet()}
            onEditPress={(item) => openBottomSheet(item)}
          />
          <VaultNotice
            baseInterestRate={vault.baseInterestRate}
            penaltyInterestRate={vault.penaltyInterestRate}
          />
          <VaultHistory transactions={transactions} />
        </ScrollView>
      )}

      <WithdrawWarningModal
        visible={showWithdrawModal}
        penaltyRate={vault?.penaltyInterestRate ?? 0.1}
        baseInterestRate={vault?.baseInterestRate ?? 3.0}
        onCancel={() => setShowWithdrawModal(false)}
        onConfirm={() => {
          setShowWithdrawModal(false);
          // TODO: 실제 출금 API 연결
        }}
      />

      {/* BottomSheet는 ScrollView 밖에 위치 */}
      <WishlistBottomSheet
        ref={bottomSheetRef}
        editTarget={editTarget}
        onSubmit={handleWishlistSubmit}
        onClose={() => setEditTarget(undefined)}
      />
    </SafeAreaView>
  );
}
