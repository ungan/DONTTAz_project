import React from 'react';
import { View, Text, TouchableOpacity, Dimensions } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import type { Wishlist } from '@/entities/vault';
import { styles } from './wishlist-slide.styles';

const { width: screenWidth } = Dimensions.get('window');
const BAR_HORIZONTAL_PADDING = 24;
const BAR_WIDTH = screenWidth - BAR_HORIZONTAL_PADDING * 2;

interface WishlistSlideProps {
  wishlists: Wishlist[];
  lockedAmount: number;
  onRegisterPress: () => void;
  onEditPress: (item: Wishlist) => void;
}

export function WishlistSlide({ wishlists, lockedAmount, onRegisterPress, onEditPress }: WishlistSlideProps) {
  const wishlist = wishlists[0];

  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Ionicons name="lock-closed" size={18} color="#111827" />
        <Text style={styles.label}>위시리스트</Text>
      </View>

      {!wishlist ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>텅 비어 있는 위시리스트...🛒</Text>
          <Text style={styles.emptyDesc}>
            목표가 생기면 돈을 지켜내는 속도가 확 달라질 거예요.{'\n'}첫 번째 목표를 세워볼까요?
          </Text>
          <TouchableOpacity style={styles.registerButton} onPress={onRegisterPress} activeOpacity={0.7}>
            <Text style={styles.registerButtonText}>등록하러 가기</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <View style={styles.wishlistContent}>
          <TouchableOpacity style={styles.editIconButton} onPress={() => onEditPress(wishlist)} activeOpacity={0.7}>
            <Ionicons name="pencil-outline" size={16} color="#9CA3AF" />
          </TouchableOpacity>

          <Text style={styles.wishName}>{wishlist.wishName}</Text>
          <Text style={styles.amountText}>
            {lockedAmount.toLocaleString('ko-KR')} / {wishlist.goalAmount.toLocaleString('ko-KR')}원
          </Text>

          {/* 프로그레스 바 */}
          {(() => {
            const progress = Math.min(lockedAmount / wishlist.goalAmount, 1);
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
