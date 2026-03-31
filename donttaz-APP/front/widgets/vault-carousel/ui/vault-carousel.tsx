import React, { useRef, useState } from 'react';
import { View, FlatList, Dimensions, NativeScrollEvent, NativeSyntheticEvent, FlatList as FlatListType } from 'react-native';
import { VaultBalance } from '@/widgets/vault-balance';
import type { Vault } from '@/entities/vault';
import { WishlistSlide } from './wishlist-slide';
import { styles } from './vault-carousel.styles';

const { width: screenWidth } = Dimensions.get('window');

interface VaultCarouselProps {
  defaultVault: Vault;
  wishlistVault: Vault | null;
  onDeposit: () => void;
  onWithdraw: () => void;
}

export function VaultCarousel({ defaultVault, wishlistVault, onDeposit, onWithdraw }: VaultCarouselProps) {
  const [activeIndex, setActiveIndex] = useState(0);
  const flatListRef = useRef<FlatListType>(null);

  const handleScroll = (e: NativeSyntheticEvent<NativeScrollEvent>) => {
    const index = Math.round(e.nativeEvent.contentOffset.x / screenWidth);
    setActiveIndex(index);
  };

  const slides = [
    <VaultBalance
      key="balance"
      lockedAmount={defaultVault.lockedAmount}
      onDeposit={onDeposit}
      onWithdraw={onWithdraw}
    />,
    <WishlistSlide
      key="wishlist"
      wishlistVault={wishlistVault}
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
