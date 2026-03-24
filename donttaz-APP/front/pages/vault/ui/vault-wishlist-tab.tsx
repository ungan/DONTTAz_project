import React, { useRef, useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import BottomSheet from '@gorhom/bottom-sheet';
import type { Vault, VaultTransaction } from '@/entities/vault';
import { createWishlistVaultApi } from '@/entities/vault';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import { WithdrawWarningModal } from '@/features/vault-withdraw';
import { VaultBalance } from '@/widgets/vault-balance';
import { VaultNotice } from '@/widgets/vault-notice';
import { VaultHistory } from '@/widgets/vault-history';
import { styles, wishlistStyles } from './vault-page.styles';

interface VaultWishlistTabProps {
  vault: Vault | null;
  transactions: VaultTransaction[];
}

export function VaultWishlistTab({ vault, transactions }: VaultWishlistTabProps) {
  const router = useRouter();
  const bottomSheetRef = useRef<BottomSheet>(null);
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);

  const handleSubmit = async (wishName: string, goalAmount: number) => {
    try {
      const res = await createWishlistVaultApi({ wishName, goalAmount });
      router.push(`/vault-memorandum?vaultId=${res.vaultId}`);
    } catch (e: any) {
      console.error('[vault-wishlist-tab] create error:', e?.response?.status, e?.response?.data);
    }
  };

  if (!vault) {
    return (
      <>
        <View style={styles.emptyContainer}>
          <View style={wishlistStyles.emptyIconWrapper}>
            <Ionicons name="cart-outline" size={52} color="#6B7280" />
          </View>
          <Text style={styles.emptyTitle}>텅 비어 있는 위시리스트..</Text>
          <Text style={styles.emptyDesc}>
            목표가 생기면 돈을 지켜내는 속도가 확 달라질 거예요.{'\n'}
            첫 번째 목표를 세워볼까요?
          </Text>
          <TouchableOpacity
            style={styles.registerButton}
            onPress={() => bottomSheetRef.current?.expand()}
            activeOpacity={0.7}
          >
            <Text style={styles.registerButtonText}>등록하러 가기</Text>
          </TouchableOpacity>
        </View>

        <WishlistBottomSheet ref={bottomSheetRef} onSubmit={handleSubmit} />
      </>
    );
  }

  const progress = vault.goalAmount > 0 ? Math.min(vault.lockedAmount / vault.goalAmount, 1) : 0;
  const progressPercent = Math.round(progress * 100);

  return (
    <>
      <ScrollView showsVerticalScrollIndicator={false}>
        <VaultBalance
          lockedAmount={vault.lockedAmount}
          onDeposit={() => router.push(`/vault-deposit?kind=WISHLIST&vaultId=${vault.vaultId}&lockedAmount=${vault.lockedAmount}`)}
          onWithdraw={() => setShowWithdrawModal(true)}
        />

        <View style={wishlistStyles.progressSection}>
          <View style={wishlistStyles.progressHeader}>
            <Text style={wishlistStyles.wishName}>{vault.wishName}</Text>
            <Text style={wishlistStyles.progressPercent}>{progressPercent}%</Text>
          </View>
          <View style={wishlistStyles.progressTrack}>
            <View style={[wishlistStyles.progressFill, { width: `${progressPercent}%` }]} />
          </View>
          <View style={wishlistStyles.progressAmounts}>
            <Text style={wishlistStyles.progressCurrent}>{vault.lockedAmount.toLocaleString('ko-KR')}원</Text>
            <Text style={wishlistStyles.progressGoal}>목표 {vault.goalAmount.toLocaleString('ko-KR')}원</Text>
          </View>
        </View>

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
          // TODO: 긴급 출금 API 연결
        }}
      />
    </>
  );
}
