import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  Easing,
} from 'react-native-reanimated';
import { SelectInput } from '@/shared/ui';
import { useYearlyReport } from '@/entities/report';
import { YearlyNag } from '@/widgets/yearly-nag';
import { YearlyReportBarChart } from './yearly-report-bar-chart';
import { YearlyReportMonthCard } from './yearly-report-month-card';
import { styles } from './yearly-report.styles';

const YEAR_OPTIONS = ['2024', '2025', '2026'];

function getCurrentYear(): string {
  return String(new Date().getFullYear());
}

export function YearlyReport() {
  const router = useRouter();
  const [selectedYear, setSelectedYear] = useState(getCurrentYear());
  const [selectedMonth, setSelectedMonth] = useState<string | null>(null);
  const { monthlyData, totalAmount, isPending, error } = useYearlyReport(selectedYear);

  const cardOpacity = useSharedValue(0);
  const cardTranslateY = useSharedValue(12);

  const cardAnimatedStyle = useAnimatedStyle(() => ({
    opacity: cardOpacity.value,
    transform: [{ translateY: cardTranslateY.value }],
  }));

  useEffect(() => {
    if (selectedMonth) {
      cardOpacity.value = 0;
      cardTranslateY.value = 12;
      cardOpacity.value = withTiming(1, { duration: 250, easing: Easing.out(Easing.ease) });
      cardTranslateY.value = withTiming(0, { duration: 250, easing: Easing.out(Easing.ease) });
    }
  }, [selectedMonth]);

  const handleYearChange = (year: string) => {
    setSelectedYear(year);
    setSelectedMonth(null);
  };

  const handleBarPress = (month: string) => {
    setSelectedMonth((prev) => (prev === month ? null : month));
  };

  const handleCardPress = () => {
    if (!selectedMonth) return;
    const yearMonth = `${selectedYear}-${selectedMonth}`;
    router.push(`/report-monthly?yearMonth=${yearMonth}`);
  };

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
        <View style={styles.yearSelector}>
          <SelectInput
            options={YEAR_OPTIONS}
            value={selectedYear}
            onChange={handleYearChange}
          />
        </View>
      </View>

      <YearlyReportBarChart
        data={monthlyData}
        selectedMonth={selectedMonth}
        onBarPress={handleBarPress}
      />

      {selectedMonth && (
        <Animated.View style={cardAnimatedStyle}>
          <YearlyReportMonthCard
            year={selectedYear}
            month={selectedMonth}
            onPress={handleCardPress}
          />
        </Animated.View>
      )}

      <YearlyNag year={selectedYear} />
    </ScrollView>
  );
}
