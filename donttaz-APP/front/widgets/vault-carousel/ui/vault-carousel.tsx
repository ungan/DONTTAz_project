import React, { useRef, useState } from 'react';
import { View, FlatList, Dimensions, NativeScrollEvent, NativeSyntheticEvent, FlatList as FlatListType } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { VaultBalance } from '@/widgets/vault-balance';
import { WishlistBottomSheet } from '@/features/vault-wishlist';
import type { Vault, Wishlist, WishlistRequest } from '@/entities/vault';
import { WishlistSlide } from './wishlist-slide';
import { styles } from './vault-carousel.styles';

const { width: screenWidth } = Dimensions.get('window');

interface VaultCarouselProps {
  vault: Vault;
  wishlists: Wishlist[];
  onDeposit: () => void;
  onWithdraw: () => void;
  onWishlistSubmit: (body: WishlistRequest, wishId?: number) => Promise<void>;
}

export function VaultCarousel({ vault, wishlists, onDeposit, onWithdraw, onWishlistSubmit }: VaultCarouselProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const [editTarget, setEditTarget] = useState<Wishlist | undefined>();
  const bottomSheetRef = useRef<BottomSheet>(null);
  const flatListRef = useRef<FlatListType>(null);

  const handleScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    const index = Math.round(e.nativeEvent.contentOffset.x / screenWidth);
    setActiveIndex(index);
  };

  const openBottomSheet = (target?: Wishlist) => {
    setEditTarget(target);
    bottomSheetRef.current?.expand();
  };

  const scrollToWishlist = () => {
    flatListRef.current?.scrollToIndex({ index: 1, animated: true });
    setActiveIndex(1);
  };

  const slides = [
    <VaultBalance
      key="balance"
      lockedAmount={vault.lockedAmount}
      onDeposit={onDeposit}
      onWithdraw={onWithdraw}
      onWishlistPress={scrollToWishlist}
    />,
    <WishlistSlide
      key="wishlist"
      wishlists={wishlists}
      onRegisterPress={() => openBottomSheet()}
      onEditPress={(item) => openBottomSheet(item)}
    />,
  ];

  return (
    <View>
      <FlatList
        ref={flatListRef}
        data={slides}
        keyExtractor={(_, i) => String(i)}
        horizontal
        pagingEnabled
        showsHorizontalScrollIndicator={false}
        onScroll={handleScroll}
        scrollEventThrottle={16}
        renderItem={({ item }) => (
          <View style={{ width: screenWidth }}>{item}</View>
        )}
      />
      <View style={styles.dotContainer}>
        {slides.map((_, i) => (
          <View key={i} style={[styles.dot, i === activeIndex && styles.dotActive]} />
        ))}
      </View>

      <WishlistBottomSheet
        ref={bottomSheetRef}
        editTarget={editTarget}
        onSubmit={onWishlistSubmit}
        onClose={() => setEditTarget(undefined)}
      />
    </View>
  );
}
