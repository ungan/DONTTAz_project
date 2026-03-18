import React from 'react';
import { View, Text } from 'react-native';
import { CategorySummary } from '@/entities/report';
import { styles } from './monthly-report-legend.styles';

interface Props {
  categories: CategorySummary[];
}

export function MonthlyReportLegend({ categories }: Props) {
  const left = categories.filter((_, i) => i % 2 === 0);
  const right = categories.filter((_, i) => i % 2 === 1);

  return (
    <View style={styles.container}>
      <View style={styles.column}>
        {left.map((cat) => (
          <View key={cat.category} style={styles.item}>
            <View style={[styles.dot, { backgroundColor: cat.color }]} />
            <Text style={styles.label}>{cat.category}</Text>
          </View>
        ))}
      </View>
      <View style={styles.column}>
        {right.map((cat) => (
          <View key={cat.category} style={styles.item}>
            <View style={[styles.dot, { backgroundColor: cat.color }]} />
            <Text style={styles.label}>{cat.category}</Text>
          </View>
        ))}
      </View>
    </View>
  );
}
