import React from 'react';
import { View, Text } from 'react-native';
import { CategorySummary } from '@/entities/report';
import { styles } from './monthly-report-legend.styles';

interface Props {
  categories: CategorySummary[];
}

export function MonthlyReportLegend({ categories }: Props) {
  return (
    <View style={styles.container}>
      {categories.map((cat) => (
        <View key={cat.category} style={styles.item}>
          <View style={[styles.dot, { backgroundColor: cat.color }]} />
          <Text style={styles.label}>{cat.category}</Text>
        </View>
      ))}
    </View>
  );
}
