import React from 'react';
import { View, Text, FlatList, TouchableOpacity, Dimensions } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import type { Wishlist } from '@/entities/vault';
import { styles } from './wishlist-slide.styles';

const { width: screenWidth } = Dimensions.get('window');
const CARD_WIDTH = screenWidth - 72;

interface WishlistSlideProps {
  wishlists: Wishlist[];
  onRegisterPress: () => void;
  onEditPress: (item: Wishlist) => void;
}

export function WishlistSlide({ wishlists, onRegisterPress, onEditPress }: WishlistSlideProps) {
  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Ionicons name="lock-closed" size={18} color="#111827" />
        <Text style={styles.label}>위시리스트</Text>
      </View>

      {wishlists.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>텅 비어있는 위시리스트 🛒</Text>
          <Text style={styles.emptyDesc}>목표가 생기면 돈을 지켜내는 속도가 확 달라질 거예요.</Text>
          <TouchableOpacity style={styles.registerButton} onPress={onRegisterPress} activeOpacity={0.7}>
            <Text style={styles.registerButtonText}>등록하러 가기</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={wishlists}
          keyExtractor={(item) => String(item.id)}
          horizontal
          showsHorizontalScrollIndicator={false}
          snapToInterval={CARD_WIDTH + 12}
          decelerationRate="fast"
          contentContainerStyle={styles.listContent}
          renderItem={({ item }) => (
            <View style={[styles.card, { width: CARD_WIDTH }]}>
              <Text style={styles.wishName}>{item.wishName}</Text>
              <Text style={styles.goalAmount}>{item.goalAmount.toLocaleString('ko-KR')}원</Text>
              <Text style={styles.targetDate}>{item.targetDate}</Text>
              <TouchableOpacity style={styles.editButton} onPress={() => onEditPress(item)} activeOpacity={0.7}>
                <Text style={styles.editButtonText}>수정하기</Text>
              </TouchableOpacity>
            </View>
          )}
        />
      )}
    </View>
  );
}
