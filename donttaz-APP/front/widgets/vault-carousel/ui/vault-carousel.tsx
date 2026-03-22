import React, { useRef, useState } from 'react';
import { View, FlatList, Dimensions, NativeScrollEvent, NativeSyntheticEvent, FlatList as FlatListType } from 'react-native';
import { VaultBalance } from '@/widgets/vault-balance';
import type { Vault, Wishlist, WishlistRequest } from '@/entities/vault';
import { WishlistSlide } from './wishlist-slide';
import { styles } from './vault-carousel.styles';

const { width: screenWidth } = Dimensions.get('window');

interface VaultCarouselProps {
  vault: Vault;
  wishlists: Wishlist[];
  onDeposit: () => void;
  onWithdraw: () => void;
  onRegisterPress: () => void;
  onEditPress: (item: Wishlist) => void;
}

export function VaultCarousel({ vault, wishlists, onDeposit, onWithdraw, onRegisterPress, onEditPress }: VaultCarouselProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const flatListRef = useRef<FlatListType>(null);

  const handleScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    const index = Math.round(e.nativeEvent.contentOffset.x / screenWidth);
    setActiveIndex(index);
  };

  const slides = [
    <VaultBalance
      key="balance"
      lockedAmount={vault.lockedAmount}
      onDeposit={onDeposit}
      onWithdraw={onWithdraw}
    />,
    <WishlistSlide
      key="wishlist"
      wishlists={wishlists}
      lockedAmount={vault.lockedAmount}
      onRegisterPress={onRegisterPress}
      onEditPress={onEditPress}
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
    </View>
  );
}
