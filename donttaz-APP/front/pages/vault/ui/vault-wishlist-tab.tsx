import React, { useRef, useState, useEffect } from 'react';
import { View, Text, Image, ScrollView, TouchableOpacity, Alert, Animated } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import Entypo from '@expo/vector-icons/Entypo';
import { useRouter } from 'expo-router';
import BottomSheet from '@gorhom/bottom-sheet';
import type { Vault, VaultStatus } from '@/entities/vault';
import { createWishlistVaultApi, terminateVaultApi } from '@/entities/vault';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import { WithdrawWarningModal } from '@/features/vault-withdraw';
import { styles, wishlistStyles } from './vault-page.styles';

const vaultFishImage = require('@/shared/assets/images/vault-fish.jpg');

const STATUS_LABEL: Record<VaultStatus, string> = {
  LOCKED: '진행 중',
  MATURED: '달성',
  BROKEN: '해지',
};

const STATUS_COLOR: Record<VaultStatus, string> = {
  LOCKED: '#5899FD',
  MATURED: '#4A9B6F',
  BROKEN: '#6B7280',
};

interface VaultWishlistTabProps {
  vault: Vault | null;
  wishlistHistory: Vault[];
  onRefetch?: () => void;
}

export function VaultWishlistTab({ vault, wishlistHistory, onRefetch }: VaultWishlistTabProps) {
  const router = useRouter();
  const bottomSheetRef = useRef<BottomSheet>(null);
  const [showWithdrawModal, setShowWithdrawModal] = useState(false);
  const floatAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.loop(
      Animated.sequence([
        Animated.timing(floatAnim, { toValue: -8, duration: 1800, useNativeDriver: true }),
        Animated.timing(floatAnim, { toValue: 0, duration: 1800, useNativeDriver: true }),
      ])
    ).start();
  }, [floatAnim]);

  const handleSubmit = async (wishName: string, goalAmount: number) => {
    try {
      const res = await createWishlistVaultApi({ wishName, goalAmount });
      router.push(`/vault-memorandum?vaultId=${res.vaultId}`);
    } catch {
    }
  };

  if (!vault) {
    return (
      <>
        <View style={styles.emptyContainer}>
          <View style={wishlistStyles.emptyIconWrapper}>
            <Ionicons name="gift-outline" size={52} color="#6B7280" />
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
        <View style={wishlistStyles.progressSection}>
          {/* 물고기 */}
          <Animated.Image
            source={vaultFishImage}
            style={[wishlistStyles.fishImage, { transform: [{ translateY: floatAnim }] }]}
            resizeMode="contain"
          />

          <View style={wishlistStyles.cardHeader}>
            <Entypo name="shopping-cart" size={18} color="#111827" />
            <Text style={wishlistStyles.cardTitle}>위시리스트</Text>
          </View>

          {/* 위시 이름 */}
          <Text style={wishlistStyles.wishName}>{vault.wishName}</Text>

          {/* 현재 / 목표 금액 */}
          <Text style={wishlistStyles.amountLine}>
            {vault.lockedAmount.toLocaleString('ko-KR')} / {vault.goalAmount.toLocaleString('ko-KR')}원
          </Text>

          {/* 진행 바 */}
          <View style={wishlistStyles.progressTrack}>
            <View style={[wishlistStyles.progressFill, { width: `${progressPercent}%` as any }]} />
          </View>

          {/* % 레이블 */}
          <View style={wishlistStyles.progressLabels}>
            <Text style={wishlistStyles.progressLabelCurrent}>{progressPercent}%</Text>
            <Text style={wishlistStyles.progressLabelGoal}>100%</Text>
          </View>

          <View style={wishlistStyles.buttonRow}>
            <TouchableOpacity
              style={[wishlistStyles.button, wishlistStyles.buttonPrimary]}
              onPress={() => router.push(`/vault-deposit?kind=WISHLIST&vaultId=${vault.vaultId}&lockedAmount=${vault.lockedAmount}&goalAmount=${vault.goalAmount}`)}
              activeOpacity={0.8}
            >
              <Text style={wishlistStyles.buttonTextPrimary}>+ 넣기</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[wishlistStyles.button, wishlistStyles.buttonOutline]}
              onPress={() => setShowWithdrawModal(true)}
              activeOpacity={0.8}
            >
              <Text style={wishlistStyles.buttonTextOutline}>긴급 출금</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={{ paddingHorizontal: 16, paddingBottom: 32, marginTop: 8, borderTopWidth: 1, borderTopColor: '#F3F4F6' }}>
          <Text style={{ fontSize: 18, fontWeight: '600', color: '#111827', paddingTop: 20, paddingBottom: 12 }}>
            지난 위시리스트
          </Text>
          {wishlistHistory.length === 0 ? (
            <View style={{ paddingVertical: 40, alignItems: 'center' }}>
              <Text style={{ fontSize: 16, color: '#9CA3AF' }}>지난 위시리스트가 없습니다.</Text>
            </View>
          ) : (
            wishlistHistory.map((item) => {
              const isMatured = item.status === 'MATURED';
              return (
                <View
                  key={item.vaultId}
                  style={{
                    borderRadius: 16,
                    marginBottom: 12,
                    shadowColor: '#000',
                    shadowOffset: { width: 0, height: 1 },
                    shadowOpacity: 0.12,
                    shadowRadius: 2,
                    elevation: 2,
                  }}
                >
                  <View style={{ borderRadius: 16, overflow: 'hidden', backgroundColor: isMatured ? '#fff' : '#F0F1F3' }}>
                  <View style={{ padding: 16 }}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                      <View style={{ flexDirection: 'row', alignItems: 'center', flex: 1, marginRight: 8, gap: 6 }}>
                        {isMatured && <Text style={{ fontSize: 15 }}>😎</Text>}
                        <Text style={{ fontSize: 16, fontWeight: '700', color: isMatured ? '#111827' : '#374151' }}>
                          {item.wishName}
                        </Text>
                      </View>
                      <Text style={{ fontSize: 13, fontWeight: '600', color: isMatured ? '#4A9B6F' : '#9CA3AF' }}>
                        {STATUS_LABEL[item.status]}
                      </Text>
                    </View>
                    <Text style={{ fontSize: 22, fontWeight: '700', color: '#111827' }}>
                      {item.goalAmount.toLocaleString('ko-KR')}원
                    </Text>
                    {item.withdrawalCount > 0 && (
                      <Text style={{ fontSize: 12, color: '#9CA3AF', marginTop: 6 }}>
                        긴급 출금 {item.withdrawalCount}회
                      </Text>
                    )}
                  </View>
                  </View>
                </View>
              );
            })
          )}
        </View>
      </ScrollView>

      <TouchableOpacity
        style={wishlistStyles.terminateButton}
        activeOpacity={0.7}
        onPress={() => {
          Alert.alert(
            '중도 해지',
            '금고를 해지하면 이자 혜택이 사라지고 수치심 메세지가 즉시 발송됩니다. \n\n정말 해지하시겠습니까?',
            [
              { text: '취소', style: 'cancel' },
              {
                text: '해지하기',
                style: 'destructive',
                onPress: async () => {
                  try {
                    await terminateVaultApi(vault.vaultId);
                    onRefetch?.();
                  } catch {
                    Alert.alert('오류', '해지 중 문제가 발생했습니다.');
                  }
                },
              },
            ]
          );
        }}
      >
        <Text style={wishlistStyles.terminateButtonText}>위시리스트 해지</Text>
      </TouchableOpacity>

      <WithdrawWarningModal
        visible={showWithdrawModal}
        baseInterestRate={vault.baseInterestRate - vault.penaltyInterestRate}
        penaltyRate={0.3}
        onCancel={() => setShowWithdrawModal(false)}
        onConfirm={() => {
          setShowWithdrawModal(false);
          router.push(`/vault-withdraw?kind=WISHLIST&vaultId=${vault.vaultId}&lockedAmount=${vault.lockedAmount}`);
        }}
      />
    </>
  );
}
