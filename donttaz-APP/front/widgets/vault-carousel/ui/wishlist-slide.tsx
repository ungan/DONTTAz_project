import React from 'react';
import { View, Text, Dimensions } from 'react-native';
import type { Vault } from '@/entities/vault';
import { styles } from './wishlist-slide.styles';

const { width: screenWidth } = Dimensions.get('window');
const BAR_HORIZONTAL_PADDING = 24;
const BAR_WIDTH = screenWidth - BAR_HORIZONTAL_PADDING * 2;

interface WishlistSlideProps {
  wishlistVault: Vault | null;
}

export function WishlistSlide({ wishlistVault }: WishlistSlideProps) {

  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Text style={styles.label}>위시리스트</Text>
      </View>

      {!wishlistVault ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>텅 비어 있는 위시리스트...🛒</Text>
          <Text style={styles.emptyDesc}>
            목표가 생기면 돈을 지켜내는 속도가 확 달라질 거예요.{'\n'}첫 번째 목표를 세워볼까요?
          </Text>
        </View>
      ) : (
        <View style={styles.wishlistContent}>
          <Text style={styles.wishName}>{wishlistVault.wishName}</Text>
          <Text style={styles.amountText}>
            {wishlistVault.lockedAmount.toLocaleString('ko-KR')} / {wishlistVault.goalAmount.toLocaleString('ko-KR')}원
          </Text>

          {(() => {
            const progress = Math.min(wishlistVault.lockedAmount / wishlistVault.goalAmount, 1);
            return (
              <View style={styles.progressWrapper}>
                <View style={styles.progressTrack}>
                  <View style={[styles.progressFill, { width: BAR_WIDTH * progress }]} />
                </View>
                <View style={styles.progressLabels}>
                  <Text style={styles.progressPercent}>{Math.round(progress * 100)}%</Text>
                  <Text style={styles.progressMax}>100%</Text>
                </View>
              </View>
            );
          })()}
        </View>
      )}
    </View>
  );
}
