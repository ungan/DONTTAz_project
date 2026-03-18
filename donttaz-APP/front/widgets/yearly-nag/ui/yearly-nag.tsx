import React from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useYearlyCategories } from '@/entities/report';
import { YearlyNagMessage } from './yearly-nag-message';
import { YearlyNagCategoryList } from './yearly-nag-category-list';
import { styles } from './yearly-nag.styles';

interface Props {
  year: string;
}

export function YearlyNag({ year }: Props) {
  const { categories, isPending, error } = useYearlyCategories(year);

  if (isPending) {
    return (
      <View style={[styles.container, styles.center]}>
        <ActivityIndicator size="small" color="#5899FD" />
      </View>
    );
  }

  if (error || categories.length === 0) {
    return (
      <View style={[styles.container, styles.center]}>
        <Text style={styles.errorText}>{error ?? '데이터가 없습니다.'}</Text>
      </View>
    );
  }

  const topCategory = categories[0].category;

  return (
    <View style={styles.container}>
      <YearlyNagMessage year={year} topCategory={topCategory} />
      <YearlyNagCategoryList categories={categories} />
    </View>
  );
}
