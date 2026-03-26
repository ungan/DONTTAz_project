import React from 'react';
import { Text, View } from 'react-native';
import type { SpendingPlanCategoryPlan } from '@/entities/plan';
import { styles } from './spending-plan-category-list.styles';

function formatWon(amount: number): string {
  return `${amount.toLocaleString('ko-KR')}원`;
}

interface SpendingPlanCategoryListProps {
  categoryPlans: SpendingPlanCategoryPlan[];
}

export function SpendingPlanCategoryList({ categoryPlans }: SpendingPlanCategoryListProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>카테고리별 상세 예산</Text>
      {categoryPlans.map((item) => (
        <View key={item.categoryName} style={styles.item}>
          <View style={styles.topRow}>
            <View style={[styles.dot, { backgroundColor: item.color }]} />
            <Text style={styles.category}>{item.categoryName}</Text>
            <Text style={styles.amount}>{formatWon(item.allocatedAmount)}</Text>
          </View>
          <Text style={styles.comment}>{item.aiComment}</Text>
        </View>
      ))}
    </View>
  );
}
