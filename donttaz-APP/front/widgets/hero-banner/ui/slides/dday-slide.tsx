import React from 'react';
import { View, Text } from 'react-native';
import { useBurndown, useBurndownStats } from '@/entities/plan';
import { styles } from './dday-slide.styles';

export function DdaySlide() {
  const now = new Date();
  const yearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  const { data } = useBurndown(yearMonth, { source: 'home-dday-slide' });
  const { daysUntilBankrupt } = useBurndownStats(data);

  const ddayText = daysUntilBankrupt !== null ? `D-${daysUntilBankrupt}` : '-';

  return (
    <View style={styles.container}>
      <Text style={styles.label}>파산일</Text>
      <Text style={styles.dday}>{ddayText}</Text>
    </View>
  );
}
