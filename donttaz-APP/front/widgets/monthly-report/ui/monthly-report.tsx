import React, { useState } from 'react';
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { useMonthlyReport } from '@/entities/report';
import { MonthlyReportDonutChart } from './monthly-report-donut-chart';
import { MonthlyReportLegend } from './monthly-report-legend';
import { MonthlyReportCategoryList } from './monthly-report-category-list';
import { styles } from './monthly-report.styles';

function getCurrentYearMonth(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

interface MonthlyReportProps {
  yearMonth?: string;
}

export function MonthlyReport({ yearMonth: yearMonthProp }: MonthlyReportProps) {
  const [yearMonth] = useState(yearMonthProp ?? getCurrentYearMonth());
  const { categories, totalAmount, isPending, error } = useMonthlyReport(yearMonth);

  const month = yearMonth.split('-')[1];

  if (isPending) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#5899FD" />
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>{error}</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.header}>
        <View>
          <Text style={styles.totalLabel}>총 금액</Text>
          <Text style={styles.totalAmount}>{totalAmount.toLocaleString()} 원</Text>
        </View>
        <View style={styles.monthBadge}>
          <Text style={styles.monthText}>{month}월</Text>
        </View>
      </View>
      <MonthlyReportDonutChart categories={categories} />
      <MonthlyReportLegend categories={categories} />
      <Text style={styles.sectionTitle}>지출 내역</Text>
      <MonthlyReportCategoryList categories={categories} />
    </ScrollView>
  );
}
