import React from 'react';
import { Text, View } from 'react-native';
import type { SpendingPlanRecommendation } from '@/entities/plan';
import { styles } from './spending-plan-recommendations.styles';

interface SpendingPlanRecommendationsProps {
  recommendations: SpendingPlanRecommendation[];
}

export function SpendingPlanRecommendations({ recommendations }: SpendingPlanRecommendationsProps) {
  if (recommendations.length === 0) {
    return null;
  }

  return (
    <View style={styles.card}>
      <Text style={styles.title}>AI 추천 포인트</Text>
      {recommendations.map((item, index) => (
        <View
          key={`${item.type}-${item.categoryName ?? 'all'}-${index}`}
          style={[styles.item, { backgroundColor: item.backgroundColor }]}
        >
          <View style={styles.itemHeader}>
            <View style={[styles.badge, { backgroundColor: '#FFFFFF' }]}>
              <Text style={[styles.badgeText, { color: item.accentColor }]}>
                {item.typeLabel}
              </Text>
            </View>
            {item.categoryName ? <Text style={styles.category}>{item.categoryName}</Text> : null}
          </View>
          <Text style={styles.message}>{item.message}</Text>
        </View>
      ))}
    </View>
  );
}
