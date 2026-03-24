import React, { useRef, useState } from 'react';
import { View, ScrollView, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import BottomSheet from '@gorhom/bottom-sheet';
import { PageHeader } from '@/shared/ui';
import { useVault } from '@/entities/vault';
import type { Wishlist, WishlistRequest } from '@/entities/vault';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import { WithdrawWarningModal } from '@/features/vault-withdraw';
import { VaultCarousel } from '@/widgets/vault-carousel';
import { VaultNotice } from '@/widgets/vault-notice';
import { VaultHistory } from '@/widgets/vault-history';
import { styles } from './vault-page.styles';

export function VaultPage() {
  const router = useRouter();
  const { defaultVault, wishlistVault, transactions, isPending } = useVault();

  const bottomSheetRef = useRef<BottomSheet>(null);
  const [editTarget, setEditTarget] = useState<Wishlist | undefined>();
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  const openBottomSheet = (target?: Wishlist) => {
    setEditTarget(target);
    bottomSheetRef.current?.expand();
  };

  const handleWishlistSubmit = async (_body: WishlistRequest, _wishId?: number) => {
    // TODO: wishlist 금고 수정 API 연결
  };

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="가상 금고" onBack={() => router.replace('/(tabs)/home')} />
      {isPending || !defaultVault ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#5899FD" />
        </View>
      ) : (
        <ScrollView showsVerticalScrollIndicator={false}>
          <VaultCarousel
            defaultVault={defaultVault}
            wishlistVault={wishlistVault}
            onDeposit={() => router.push(`/vault-deposit?lockedAmount=${defaultVault.lockedAmount}`)}
            onWithdraw={() => setShowWithdrawModal(true)}
          />
          <VaultNotice
            baseInterestRate={defaultVault.baseInterestRate}
            penaltyInterestRate={defaultVault.penaltyInterestRate}
          />
          <VaultHistory transactions={transactions} />
        </ScrollView>
      )}

      <WithdrawWarningModal
        visible={showWithdrawModal}
        penaltyRate={defaultVault?.penaltyInterestRate ?? 0.1}
        baseInterestRate={defaultVault?.baseInterestRate ?? 3.0}
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
