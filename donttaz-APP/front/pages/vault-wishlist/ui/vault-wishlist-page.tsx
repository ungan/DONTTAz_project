import React, { useRef, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, Dimensions, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import BottomSheet from '@gorhom/bottom-sheet';
import { Ionicons } from '@expo/vector-icons';
import { PageHeader } from '@/shared/ui';
import { useVault, useWishlist } from '@/entities/vault';
import type { Wishlist, WishlistRequest } from '@/entities/vault';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import { styles } from './vault-wishlist-page.styles';

const { width: screenWidth } = Dimensions.get('window');
const CARD_WIDTH = screenWidth - 72;

export function VaultWishlistPage() {
  const { vault, isPending } = useVault();
  const { wishlists, post, patch } = useWishlist(vault?.vaultId ?? null);
  const bottomSheetRef = useRef<BottomSheet>(null);
  const [editTarget, setEditTarget] = useState<Wishlist | undefined>();

  const openBottomSheet = (target?: Wishlist) => {
    setEditTarget(target);
    bottomSheetRef.current?.expand();
  };

  const handleSubmit = async (body: WishlistRequest, wishId?: number) => {
    if (wishId) {
      await patch(wishId, body);
    } else {
      await post(body);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader
        title="위시리스트"
        rightAction={
          <TouchableOpacity onPress={() => openBottomSheet()} activeOpacity={0.7}>
            <Ionicons name="add" size={24} color="#111827" />
          </TouchableOpacity>
        }
      />

      {isPending ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#5899FD" />
        </View>
      ) : wishlists.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>텅 비어있는 위시리스트 🛒</Text>
          <Text style={styles.emptyDesc}>
            목표가 생기면 돈을 지켜내는 속도가 확 달라질 거예요.{'\n'}
            첫 번째 목표를 세워볼까요?
          </Text>
          <TouchableOpacity style={styles.registerButton} onPress={() => openBottomSheet()} activeOpacity={0.7}>
            <Text style={styles.registerButtonText}>등록하러 가기</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={wishlists}
          keyExtractor={(item) => String(item.id)}
          horizontal
          showsHorizontalScrollIndicator={false}
          snapToInterval={CARD_WIDTH + 16}
          decelerationRate="fast"
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <View style={[styles.card, { width: CARD_WIDTH }]}>
              <Text style={styles.wishName}>{item.wishName}</Text>
              <Text style={styles.goalAmount}>{item.goalAmount.toLocaleString('ko-KR')}원</Text>
              <Text style={styles.targetDate}>목표일 {item.targetDate}</Text>
              <TouchableOpacity
                style={styles.editButton}
                onPress={() => openBottomSheet(item)}
                activeOpacity={0.7}
              >
                <Text style={styles.editButtonText}>수정하기</Text>
              </TouchableOpacity>
            </View>
          )}
        />
      )}

      <WishlistBottomSheet
        ref={bottomSheetRef}
        editTarget={editTarget}
        onSubmit={handleSubmit}
        onClose={() => setEditTarget(undefined)}
      />
    </SafeAreaView>
  );
}
